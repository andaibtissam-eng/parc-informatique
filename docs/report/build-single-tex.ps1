$ErrorActionPreference = 'Stop'

$root = 'C:\Users\andai\Desktop\parc_informatique\docs\report'
$mainPath = Join-Path $root 'main.tex'
$outPath = Join-Path $root 'main-all-in-one.tex'

$main = Get-Content -Raw -Encoding UTF8 $mainPath

$bib = @'
\begin{thebibliography}{99}
\bibitem{pressman2014} Roger S. Pressman and Bruce R. Maxim, \textit{Software Engineering: A Practitioner's Approach}, 8th ed., McGraw-Hill, 2014.
\bibitem{sommerville2015} Ian Sommerville, \textit{Software Engineering}, 10th ed., Pearson, 2015.
\bibitem{schwaber2020} Ken Schwaber and Jeff Sutherland, \textit{The Scrum Guide}, Scrum.org, 2020.
\bibitem{fielding2000} Roy Thomas Fielding, \textit{Architectural Styles and the Design of Network-based Software Architectures}, PhD thesis, University of California, Irvine, 2000.
\bibitem{reactdocs} React Team, \textit{React Documentation}, available online: \url{https://react.dev/}.
\bibitem{springdocs} Spring Team, \textit{Spring Boot Reference Documentation}, available online: \url{https://docs.spring.io/spring-boot/docs/current/reference/html/}.
\bibitem{postgresdocs} PostgreSQL Global Development Group, \textit{PostgreSQL Documentation}, available online: \url{https://www.postgresql.org/docs/}.
\bibitem{dockerdocs} Docker Inc., \textit{Docker Documentation}, available online: \url{https://docs.docker.com/}.
\bibitem{merkel2014docker} Dirk Merkel, ``Docker: lightweight Linux containers for consistent development and deployment,'' \textit{Linux Journal}, no. 239, 2014.
\bibitem{owasp2021} OWASP Foundation, \textit{OWASP Top 10: The Ten Most Critical Web Application Security Risks}, 2021.
\bibitem{fowler2002} Martin Fowler, \textit{Patterns of Enterprise Application Architecture}, Addison-Wesley, 2002.
\bibitem{booch2005} Grady Booch, James Rumbaugh and Ivar Jacobson, \textit{The Unified Modeling Language User Guide}, 2nd ed., Addison-Wesley, 2005.
\end{thebibliography}
'@

function Resolve-InlineFile([string]$target) {
    $filename = if ($target -like '*.tex') { $target } else { "$target.tex" }
    $candidates = @(
        (Join-Path $root $filename),
        (Join-Path (Join-Path $root 'chapters') $filename),
        (Join-Path (Join-Path $root 'figures') $filename),
        (Join-Path (Join-Path $root 'appendices') $filename)
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) { return $candidate }
    }

    throw "Missing input file: $target"
}

function Inline-Inputs([string]$content) {
    $pattern = '\\input\{([^}]+)\}'
    while ($content -match $pattern) {
        $content = [regex]::Replace($content, $pattern, {
            param($m)
            $target = $m.Groups[1].Value
            $file = Resolve-InlineFile $target
            $raw = Get-Content -Raw -Encoding UTF8 $file
            "`r`n% >>> BEGIN INLINE: $target`r`n$raw`r`n% <<< END INLINE: $target`r`n"
        })
    }
    return $content
}

$macroMap = @{
    'fileAuthStore'      = Join-Path $root 'sources/client/src/features/auth/auth.store.js'
    'fileApiHttp'        = Join-Path $root 'sources/client/src/api/http.js'
    'fileDashboardPage'  = Join-Path $root 'sources/client/src/pages/dashboard/DashboardPage.jsx'
    'fileSecurityConfig' = Join-Path $root 'sources/spring/src/main/java/com/parcinformatique/app/config/SecurityConfig.java'
    'fileJwtProvider'    = Join-Path $root 'sources/spring/src/main/java/com/parcinformatique/app/security/JwtTokenProvider.java'
    'fileWebSocketConfig'= Join-Path $root 'sources/spring/src/main/java/com/parcinformatique/app/websocket/WebSocketConfig.java'
    'fileSpringAppYml'   = Join-Path $root 'sources/spring/src/main/resources/application.yml'
    'fileDockerCompose'  = Join-Path $root 'sources/spring/docker-compose.yml'
    'fileDockerfile'     = Join-Path $root 'sources/spring/Dockerfile'
    'fileInitSql'        = Join-Path $root 'sources/spring/database/01-init.sql'
    'fileSeedSql'        = Join-Path $root 'sources/spring/database/02-seed-demo-data.sql'
    'filePostman'        = Join-Path $root 'sources/spring/postman/ParcFlow.postman_collection.json'
    'filePom'            = Join-Path $root 'sources/spring/pom.xml'
    'fileSeeder'         = Join-Path $root 'sources/spring/src/main/java/com/parcinformatique/app/config/DataSeeder.java'
    'fileSpringTest'     = Join-Path $root 'sources/spring/src/test/java/com/parcinformatique/app/ParcInformatiqueApplicationTests.java'
}

function Inline-Listings([string]$content) {
    $pattern = '\\lstinputlisting(?:\[([^\]]*)\])?\{\\([A-Za-z0-9]+)\}'
    return [regex]::Replace($content, $pattern, {
        param($m)
        $macroName = $m.Groups[2].Value
        if (-not $macroMap.ContainsKey($macroName)) {
            throw "Missing macro mapping for listing: $macroName"
        }
        $file = $macroMap[$macroName]
        if (-not (Test-Path $file)) {
            throw "Missing source file for listing: $file"
        }
        "\begin{keybox}[Extrait technique]`r`nLe code complet est disponible dans les sources du projet. Le rapport conserve ici l'explication technique afin de garder une mise en page claire, lisible et adaptée à l'impression.`r`n\end{keybox}"
    })
}

$main = Inline-Inputs $main
$main = Inline-Listings $main
$main = [regex]::Replace($main, '\\bibliographystyle\{[^}]+\}\s*\\bibliography\{[^}]+\}', [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $bib })
Set-Content -Path $outPath -Value $main -Encoding UTF8
Write-Output "Generated: $outPath"
