# Compilation du rapport

Le rapport principal se trouve dans `main.tex`.

Le fichier le plus simple a compiler est `main-all-in-one.tex`, car il contient deja la bibliographie integree.

## Prerequis

- Une distribution LaTeX complete : `TeX Live` ou `MiKTeX`
- Un compilateur PDFLaTeX

## Commande recommandee

```bash
latexmk -pdf main-all-in-one.tex
```

Si `latexmk` n'est pas disponible :

```bash
pdflatex main-all-in-one.tex
pdflatex main-all-in-one.tex
```

## Si erreur `output.aux` / `abx@aux@refcontext`

Cette erreur vient presque toujours d'un ancien fichier auxiliaire genere par `biblatex`.
Elle ne signifie pas que le rapport est vide.

Solution locale :

```bash
latexmk -C
pdflatex main-all-in-one.tex
pdflatex main-all-in-one.tex
```

Solution sur Overleaf ou compilateur en ligne :

- cliquer sur `Recompile from scratch`;
- ou supprimer les fichiers auxiliaires `output.aux`, `output.bbl`, `output.bcf`, `output.blg`, `output.run.xml`;
- puis compiler `main-all-in-one.tex`.

## Compilation de `main.tex`

`main.tex` utilise BibTeX :

```bash
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

## Personnalisation rapide

Les metadonnees principales sont definies au debut de `main.tex` :

- `\supervisorname`
- `\filiere`
- `\departmentname`
- `\cityname`
- `\reportdate`
- `\academicYear`

Les logos de la page de garde sont stockes dans :

- `assets/logos/emsi-logo.png`
- `assets/logos/honoris-logo.png`
