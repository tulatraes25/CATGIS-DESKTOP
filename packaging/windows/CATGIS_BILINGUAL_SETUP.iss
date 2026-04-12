#ifndef AppImageDir
  #error "AppImageDir define is required."
#endif
#ifndef OutputDir
  #error "OutputDir define is required."
#endif
#ifndef InstallerName
  #define InstallerName "CATGIS Desktop"
#endif
#ifndef InstallerVersion
  #define InstallerVersion "1.0.0"
#endif
#ifndef InstallerPublisher
  #define InstallerPublisher "Lic Claudio Alejandro Tula"
#endif
#ifndef InstallerExeName
  #define InstallerExeName "CATGIS-Desktop-1.0.0"
#endif
#ifndef InstallerLicense
  #error "InstallerLicense define is required."
#endif
#ifndef InstallerIcon
  #error "InstallerIcon define is required."
#endif
#ifndef InstallerGroup
  #define InstallerGroup "CATGIS"
#endif
#ifndef InstallerDir
  #define InstallerDir "CATGIS Desktop"
#endif

[Setup]
AppId={{9A80600A-B8E7-4D30-B96E-1C3BF571D6B4}
AppName={#InstallerName}
AppVersion={#InstallerVersion}
AppPublisher={#InstallerPublisher}
DefaultDirName={autopf}\{#InstallerDir}
DefaultGroupName={#InstallerGroup}
LicenseFile={#InstallerLicense}
OutputDir={#OutputDir}
OutputBaseFilename={#InstallerExeName}
SetupIconFile={#InstallerIcon}
UninstallDisplayIcon={app}\{#InstallerName}.exe
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ShowLanguageDialog=auto
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64compatible
WizardResizable=yes
DisableProgramGroupPage=yes

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[CustomMessages]
english.AppLanguagePageTitle=Default CATGIS language
english.AppLanguagePageSubtitle=Choose the initial language for the installed application.
english.AppLanguagePageDescription=This selection is saved as the initial CATGIS language. The user can change it later from the Help menu.
english.AppLanguageSpanish=Spanish
english.AppLanguageEnglish=English
english.AppLanguageWriteError=The default CATGIS language could not be written to the installation folder.
spanish.AppLanguagePageTitle=Idioma inicial de CATGIS
spanish.AppLanguagePageSubtitle=Elegi el idioma con el que arrancara la aplicacion instalada.
spanish.AppLanguagePageDescription=Esta seleccion se guarda como idioma inicial de CATGIS. Luego se puede cambiar desde el menu Ayuda.
spanish.AppLanguageSpanish=Espanol
spanish.AppLanguageEnglish=English
spanish.AppLanguageWriteError=No se pudo guardar el idioma inicial de CATGIS en la carpeta de instalacion.

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "{#AppImageDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#InstallerGroup}\{#InstallerName}"; Filename: "{app}\{#InstallerName}.exe"
Name: "{autodesktop}\{#InstallerName}"; Filename: "{app}\{#InstallerName}.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\{#InstallerName}.exe"; Description: "{cm:LaunchProgram,{#InstallerName}}"; Flags: nowait postinstall skipifsilent

[Code]
var
  AppLanguagePage: TInputOptionWizardPage;

procedure InitializeWizard;
begin
  AppLanguagePage :=
    CreateInputOptionPage(
      wpLicense,
      ExpandConstant('{cm:AppLanguagePageTitle}'),
      ExpandConstant('{cm:AppLanguagePageSubtitle}'),
      ExpandConstant('{cm:AppLanguagePageDescription}'),
      False,
      False);

  AppLanguagePage.Add(ExpandConstant('{cm:AppLanguageSpanish}'));
  AppLanguagePage.Add(ExpandConstant('{cm:AppLanguageEnglish}'));
  AppLanguagePage.Values[0] := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  DefaultLanguage: string;
  DefaultsPath: string;
begin
  if CurStep = ssPostInstall then
  begin
    if AppLanguagePage.Values[1] then
      DefaultLanguage := 'en'
    else
      DefaultLanguage := 'es';

    DefaultsPath := ExpandConstant('{app}\app\catgis-defaults.properties');
    if not SaveStringToFile(DefaultsPath, 'ui.language=' + DefaultLanguage + #13#10, False) then
      MsgBox(ExpandConstant('{cm:AppLanguageWriteError}'), mbError, MB_OK);
  end;
end;
