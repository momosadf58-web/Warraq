# Bereitet Coverbilder für den PC-Test der arabischen Erkennung vor (PaddleArabicTest).
# Aufruf: .\OCR-Testbilder.ps1 -Quelle <Ordner mit jpg/png> -Ziel <Ausgabeordner>
# Erzeugt zusätzlich ein künstliches Gold-auf-Dunkel-Cover mit arabischem Titel/Autor.
param([Parameter(Mandatory)][string]$Quelle,[Parameter(Mandatory)][string]$Ziel)
Add-Type -AssemblyName System.Drawing
New-Item -ItemType Directory -Force $Ziel | Out-Null

function Save-Argb([System.Drawing.Bitmap]$bmp,[string]$path){
    # Größere Bilder wie in der App auf ca. 1600 px begrenzen.
    $scale=[Math]::Min(1.0,1600.0/[Math]::Max($bmp.Width,$bmp.Height))
    if($scale -lt 1){$bmp=New-Object System.Drawing.Bitmap($bmp,[int]($bmp.Width*$scale),[int]($bmp.Height*$scale))}
    $rect=New-Object System.Drawing.Rectangle(0,0,$bmp.Width,$bmp.Height)
    $data=$bmp.LockBits($rect,[System.Drawing.Imaging.ImageLockMode]::ReadOnly,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $bytes=New-Object byte[] ($data.Stride*$bmp.Height)
    [Runtime.InteropServices.Marshal]::Copy($data.Scan0,$bytes,0,$bytes.Length);$bmp.UnlockBits($data)
    $out=New-Object IO.BinaryWriter([IO.File]::Create($path))
    # Big endian; das Komma verhindert, dass PowerShell das Byte-Array in Einzelwerte zerlegt.
    $be={param($v)$b=[BitConverter]::GetBytes([int]$v);[Array]::Reverse($b);,$b}
    $out.Write([byte[]](& $be $bmp.Width));$out.Write([byte[]](& $be $bmp.Height))
    $buf=New-Object byte[] ($bmp.Width*$bmp.Height*4);$i=0
    for($y=0;$y -lt $bmp.Height;$y++){$row=$y*$data.Stride;for($x=0;$x -lt $bmp.Width;$x++){$p=$row+$x*4
        $buf[$i]=$bytes[$p+3];$buf[$i+1]=$bytes[$p+2];$buf[$i+2]=$bytes[$p+1];$buf[$i+3]=$bytes[$p];$i+=4}}
    $out.Write($buf);$out.Close()
}

Get-ChildItem $Quelle -File | Where-Object {$_.Extension -match '^\.(jpe?g|png)$'} | ForEach-Object {
    $img=[System.Drawing.Image]::FromFile($_.FullName)
    # EXIF-Drehung wie in der App berücksichtigen.
    if($img.PropertyIdList -contains 0x0112){switch($img.GetPropertyItem(0x0112).Value[0]){3{$img.RotateFlip('Rotate180FlipNone')}6{$img.RotateFlip('Rotate90FlipNone')}8{$img.RotateFlip('Rotate270FlipNone')}}}
    Save-Argb (New-Object System.Drawing.Bitmap($img)) (Join-Path $Ziel ($_.BaseName+'.argb'));$img.Dispose()
}

$bmp=New-Object System.Drawing.Bitmap(600,900);$g=[System.Drawing.Graphics]::FromImage($bmp)
$g.TextRenderingHint='AntiAliasGridFit';$g.Clear([System.Drawing.Color]::FromArgb(28,32,40))
$gold=New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(214,171,85))
$fmt=New-Object System.Drawing.StringFormat;$fmt.Alignment='Center';$fmt.FormatFlags='DirectionRightToLeft'
# Arabisch über Codepunkte: PowerShell 5.1 liest Skripte ohne BOM als ANSI.
$title=-join [char[]](0x627,0x644,0x623,0x64A,0x627,0x645)             # الأيام
$author=-join [char[]](0x637,0x647,0x20,0x62D,0x633,0x64A,0x646)        # طه حسين
$g.DrawString($title,(New-Object System.Drawing.Font('Arial',64,[System.Drawing.FontStyle]::Bold)),$gold,(New-Object System.Drawing.RectangleF(0,260,600,140)),$fmt)
$g.DrawString($author,(New-Object System.Drawing.Font('Arial',36)),$gold,(New-Object System.Drawing.RectangleF(0,600,600,90)),$fmt)
$bmp.Save((Join-Path $Ziel 'kuenstlich-al-ayyam.png'))
$g.Dispose();Save-Argb $bmp (Join-Path $Ziel 'kuenstlich-al-ayyam.argb')
Get-ChildItem $Ziel -Filter *.argb | ForEach-Object {$_.Name}
