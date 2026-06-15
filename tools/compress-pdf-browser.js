const fs = require("fs");
const http = require("http");
const path = require("path");
const { chromium } = require("playwright");
const { PDFDocument } = require("pdf-lib");

const [input, output, scaleArg = "1.35", qualityArg = "0.78"] = process.argv.slice(2);

if (!input || !output) {
  console.error("Usage: node tools/compress-pdf-browser.js input.pdf output.pdf [scale] [jpegQuality]");
  process.exit(1);
}

const scale = Number(scaleArg);
const jpegQuality = Number(qualityArg);
const pdfjsPath = require.resolve("pdfjs-dist/build/pdf.mjs");
const pdfjsDir = path.dirname(pdfjsPath);

function dataUrlToBytes(dataUrl) {
  return Buffer.from(dataUrl.split(",")[1], "base64");
}

async function main() {
  const sourceBytes = fs.readFileSync(input);
  const sourceB64 = sourceBytes.toString("base64");
  const server = http.createServer((req, res) => {
    const requested = decodeURIComponent(req.url.split("?")[0]).replace(/^\/+/, "");
    const filePath = path.join(pdfjsDir, requested || "pdf.mjs");
    if (!filePath.startsWith(pdfjsDir) || !fs.existsSync(filePath)) {
      res.writeHead(404);
      res.end("not found");
      return;
    }
    const ext = path.extname(filePath);
    res.writeHead(200, {
      "Content-Type": ext === ".mjs" || ext === ".js" ? "text/javascript" : "application/octet-stream",
      "Access-Control-Allow-Origin": "*"
    });
    fs.createReadStream(filePath).pipe(res);
  });
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const pdfjsUrl = `http://127.0.0.1:${port}/pdf.mjs`;

  const launchOptions = { headless: true };
  if (process.env.CHROME_PATH) {
    launchOptions.executablePath = process.env.CHROME_PATH;
  }
  const browser = await chromium.launch(launchOptions);
  const page = await browser.newPage();
  page.on("pageerror", (error) => console.error("PAGE ERROR:", error.message));
  page.on("console", (message) => {
    if (message.type() === "error") console.error("BROWSER:", message.text());
  });

  await page.setContent(`<!doctype html>
    <html>
      <body><canvas id="canvas"></canvas></body>
      <script type="module">
        import * as pdfjsLib from "${pdfjsUrl}";
        pdfjsLib.GlobalWorkerOptions.workerSrc = "http://127.0.0.1:${port}/pdf.worker.mjs";
        globalThis.pdfjsLib = pdfjsLib;
      </script>
    </html>`);

  await page.waitForFunction(() => globalThis.pdfjsLib);

  await page.evaluate(async (b64) => {
    const binary = atob(b64);
    const data = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i += 1) data[i] = binary.charCodeAt(i);
    globalThis.loadedPdf = await globalThis.pdfjsLib.getDocument({
      data,
      disableFontFace: false,
      useSystemFonts: true
    }).promise;
  }, sourceB64);

  const pageCount = await page.evaluate(() => globalThis.loadedPdf.numPages);
  const outPdf = await PDFDocument.create();

  for (let pageNumber = 1; pageNumber <= pageCount; pageNumber += 1) {
    const rendered = await page.evaluate(async ({ pageNumber, scale, jpegQuality }) => {
      const pdfPage = await globalThis.loadedPdf.getPage(pageNumber);
      const viewport = pdfPage.getViewport({ scale });
      const canvas = document.getElementById("canvas");
      const context = canvas.getContext("2d", { alpha: false });
      canvas.width = Math.ceil(viewport.width);
      canvas.height = Math.ceil(viewport.height);
      context.fillStyle = "white";
      context.fillRect(0, 0, canvas.width, canvas.height);
      await pdfPage.render({ canvasContext: context, viewport }).promise;
      const dataUrl = canvas.toDataURL("image/jpeg", jpegQuality);
      const unitViewport = pdfPage.getViewport({ scale: 1 });
      return {
        dataUrl,
        width: unitViewport.width,
        height: unitViewport.height
      };
    }, { pageNumber, scale, jpegQuality });

    const jpg = await outPdf.embedJpg(dataUrlToBytes(rendered.dataUrl));
    const pdfPage = outPdf.addPage([rendered.width, rendered.height]);
    pdfPage.drawImage(jpg, {
      x: 0,
      y: 0,
      width: rendered.width,
      height: rendered.height
    });

    if (pageNumber % 10 === 0 || pageNumber === pageCount) {
      console.log(`Rendered ${pageNumber}/${pageCount}`);
    }
  }

  await browser.close();
  server.close();

  const outBytes = await outPdf.save({ useObjectStreams: true, objectsPerTick: 200 });
  fs.mkdirSync(path.dirname(path.resolve(output)), { recursive: true });
  fs.writeFileSync(output, outBytes);

  const mb = (n) => (n / 1024 / 1024).toFixed(2);
  console.log(`Input: ${mb(sourceBytes.length)} MB`);
  console.log(`Output: ${mb(outBytes.length)} MB`);
  console.log(path.resolve(output));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
