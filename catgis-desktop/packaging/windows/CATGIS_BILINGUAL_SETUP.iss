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

[Code]
const
  LegacyMsiUninstallKey = 'Software\Microsoft\Windows\CurrentVersion\Uninstall';

function TryFindLegacyMsiProductCode(var ProductCode: string): Boolean;
var
  SubKeys: TArrayOfString;
  I: Integer;
  DisplayName: string;
  UninstallString: string;
  CandidateKey: string;
begin
  Result := False;
  ProductCode := '';

  if not RegGetSubkeyNames(HKLM64, LegacyMsiUninstallKey, SubKeys) then
    exit;

  for I := 0 to GetArrayLength(SubKeys) - 1 do
  begin
    CandidateKey := LegacyMsiUninstallKey + '\' + SubKeys[I];
    if RegQueryStringValue(HKLM64, CandidateKey, 'DisplayName', DisplayName) and
       (CompareText(Trim(DisplayName), '{#InstallerName}') = 0) and
       RegQueryStringValue(HKLM64, CandidateKey, 'UninstallString', UninstallString) and
       (Pos('MsiExec.exe', UninstallString) > 0) and
       (Length(SubKeys[I]) > 2) and
       (SubKeys[I][1] = '{') and
       (SubKeys[I][Length(SubKeys[I])] = '}') then
    begin
      ProductCode := SubKeys[I];
      Result := True;
      exit;
    end;
  end;
end;

procedure UninstallLegacyMsiIfPresent;
var
  ProductCode: string;
  ResultCode: Integer;
begin
  if not TryFindLegacyMsiProductCode(ProductCode) then
    exit;

  Log(Format('Se detectó una instalación MSI heredada de %s con ProductCode=%s. Se desinstalará antes de continuar.', ['{#InstallerName}', ProductCode]));
  if not Exec(
    ExpandConstant('{sys}\msiexec.exe'),
    '/x ' + ProductCode + ' /qn /norestart',
    '',
    SW_HIDE,
    ewWaitUntilTerminated,
    ResultCode
  ) then
  begin
    RaiseException('No se pudo iniciar la desinstalación de la instalación MSI heredada de CATGIS Desktop.');
  end;

  if (ResultCode <> 0) and (ResultCode <> 1605) and (ResultCode <> 3010) then
  begin
    RaiseException(Format('La desinstalación de la instalación MSI heredada devolvió código %d.', [ResultCode]));
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssInstall then
    UninstallLegacyMsiIfPresent;
end;

[Run]
Filename: "{app}\CATGIS Desktop.exe"; Description: "Iniciar CATGIS Desktop"; Flags: nowait postinstall skipifsilent
