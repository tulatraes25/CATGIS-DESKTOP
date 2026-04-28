#ifndef AppImageDir
  #error AppImageDir no definido
#endif
#ifndef OutputDir
  #error OutputDir no definido
#endif
#ifndef InstallerName
  #define InstallerName "CATGIS Desktop"
#endif
#ifndef InstallerVersion
  #define InstallerVersion "1.0.0"
#endif
#ifndef InstallerPublisher
  #define InstallerPublisher "CATGIS"
#endif
#ifndef InstallerExeName
  #define InstallerExeName "CATGIS Desktop"
#endif
#ifndef InstallerLicense
  #define InstallerLicense ""
#endif
#ifndef InstallerIcon
  #define InstallerIcon ""
#endif
#ifndef InstallerGroup
  #define InstallerGroup "CATGIS"
#endif
#ifndef InstallerDir
  #define InstallerDir "CATGIS Desktop"
#endif

[Setup]
AppId={{3A8AA88D-9EC8-4F91-9C65-7F86A28CB4F5}
AppName={#InstallerName}
AppVersion={#InstallerVersion}
AppPublisher={#InstallerPublisher}
DefaultDirName={autopf}\{#InstallerDir}
DefaultGroupName={#InstallerGroup}
DisableProgramGroupPage=yes
LicenseFile={#InstallerLicense}
OutputDir={#OutputDir}
OutputBaseFilename={#InstallerExeName}
Compression=lzma
SolidCompression=yes
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin
UninstallDisplayIcon={app}\CATGIS Desktop.exe
#if InstallerIcon != ""
SetupIconFile={#InstallerIcon}
#endif

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "{#AppImageDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#InstallerName}"; Filename: "{app}\CATGIS Desktop.exe"
Name: "{autodesktop}\{#InstallerName}"; Filename: "{app}\CATGIS Desktop.exe"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Crear acceso directo en el escritorio"; GroupDescription: "Accesos directos:"

[Run]
Filename: "{app}\CATGIS Desktop.exe"; Description: "Iniciar CATGIS Desktop"; Flags: nowait postinstall skipifsilent
