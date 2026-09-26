# Generates the small, code-drawn chart/book icon used by the window and Windows launcher.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$iconDirectory = Join-Path $root 'src\main\resources\icons'
New-Item -ItemType Directory -Force $iconDirectory | Out-Null
$bitmap = New-Object System.Drawing.Bitmap 256,256
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear([System.Drawing.Color]::FromArgb(28,55,86))
$white = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$teal = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(68,211,183))
$pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::White),10
$graphics.DrawLine($pen,48,205,208,205)
$graphics.FillRectangle($teal,60,143,30,48)
$graphics.FillRectangle($teal,111,111,30,80)
$graphics.FillRectangle($teal,162,79,30,112)
$graphics.FillPolygon($white,[System.Drawing.Point[]]@(
    [System.Drawing.Point]::new(40,58),[System.Drawing.Point]::new(106,30),
    [System.Drawing.Point]::new(172,58),[System.Drawing.Point]::new(106,86)))
$graphics.DrawLine($pen,60,72,60,112)
$png = Join-Path $iconDirectory 'academic-tracker.png'
$bitmap.Save($png,[System.Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose()
$bitmap.Dispose()
$white.Dispose()
$teal.Dispose()
$pen.Dispose()
$pngBytes = [System.IO.File]::ReadAllBytes($png)
$stream = [System.IO.File]::Create((Join-Path $iconDirectory 'academic-tracker.ico'))
$writer = New-Object System.IO.BinaryWriter $stream
try {
    $writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]1)
    $writer.Write([byte]0); $writer.Write([byte]0); $writer.Write([byte]0); $writer.Write([byte]0)
    $writer.Write([uint16]1); $writer.Write([uint16]32)
    $writer.Write([uint32]$pngBytes.Length); $writer.Write([uint32]22)
    $writer.Write($pngBytes)
} finally { $writer.Dispose(); $stream.Dispose() }
