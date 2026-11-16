# Inventario de ramas remotas — saneamiento 2026-11-15

Status: registro de ejecucion (no es un spec).
Base: `main` = `a64de322e028de2f9194ae33f48535d886886da5`.

## Resumen

| Momento | Ramas remotas |
|---|---|
| Antes (auditoria 2026-11-15) | 160 |
| Despues del borrado | 47 (`main` + 46 historicas sin mergear) |

Se borraron **113** ramas que ya estaban contenidas en `main`
(ancestro confirmado con `git merge-base --is-ancestor`). Cada SHA quedo
registrado abajo; el contenido de esas ramas vive en el historial de `main`.
Las **46** restantes no estan contenidas y se conservan para triage:
son intentos historicos de trabajos ya rescatados/superados o evidencia A0.

## Ramas borradas (113)

| SHA | Rama |
|---|---|
| `410be15efaf37f3543beb0bf12e94047d3e4c9bc` | `docs/audit-spec-closure` |
| `d34b4c42a9920dfb12f3f8879839d77da3def23b` | `docs/catgis-engineering-spec-kiro-skill` |
| `fc9ce3b0ce1cb73d2b064b7c0b3106c36e7a9c77` | `docs/catgis-github-first-opencode` |
| `f988315e12dd1d09758b261ed390d126ba5ba788` | `docs/gso-close-certified` |
| `97ef3924b19bcad5b004fb4fd8a9d98ab96507e7` | `fix/attestation-live-main-guard` |
| `19d07f332393b445ece2bb4fec4b41fa670a583c` | `fix/batch-dialog-flow-post133` |
| `ecaaab157591da04625eeb2b2528cc87067f9b37` | `fix/catmap-atlas-edt-render` |
| `a5947022403658cff934f8fea458e09f958d1522` | `fix/catserver-adaptive-database-connect` |
| `df0917f7d9fc9c24980441e7bc7f23581ecbe7c9` | `fix/catserver-ejido-mask-fail-closed` |
| `32ee12ea402c22db09b23f6a2dd8b6952c671006` | `fix/crs-measurement-envelope-failclosed-post160` |
| `4dc8238ac60fe3c96a7e2b1ef32e57b975b687b7` | `fix/fgb-fail-closed-eager-containment` |
| `54809eff033badf469ba1dc97f2a1f4082a61d1b` | `fix/fgb-header-crs-clean-main-20261017` |
| `c5ecd5d939ba5e7f9be83b8a6428041fa4e56b84` | `fix/fgb-open-source-crs-provenance-20261017` |
| `2c9bc780421b07f420e2ca7dd61fcc56688c6228` | `fix/fgb-unknown-crs-current-main-20261017` |
| `c4e2f1b63973c6653d37ae3381b990f8bf91bd4c` | `fix/las-integrity-on-certified-main-20261017` |
| `be3726191d61fa21e8c20e2aa76fbf815bb62eaa` | `fix/las-source-crs-project-roundtrip-20261017` |
| `e46c1be154213fea391b09ba2c09785db240f93a` | `fix/las-unknown-crs-failclosed-20261017` |
| `9e1f24ca8112296e71328d1b47493ff69029c7fc` | `fix/main-audit` |
| `ddd5ecee608952b5140c617b9b2239380eaa387c` | `fix/opentopography-request-integrity-post125` |
| `a2046426254d6da20618ae6638304288a1bf7862` | `fix/postgis-unsafe-replace-disabled` |
| `716d0e3b791d3591838bfefe516586475582388d` | `fix/project-atomic-save-post133` |
| `0f39461f1bb210422f6a4f52ceddf0329a1e0acf` | `fix/project-open-bounded-precommit-parser` |
| `2b915ceb118892148f4892c2d0cbb9186aefe20f` | `fix/project-staged-open-post136` |
| `d90462c8a04052ffdd7287e4e4600d64a47df52d` | `fix/raster-band-integrity-post133` |
| `f40f4f345993ee07f434ae3978467e7bfbab30a2` | `fix/raster-reclass-integrity-post136` |
| `46e0d5218449777d06bf0c98dde1e7559b2f5fc0` | `fix/raster-sidecar-validation-post125` |
| `5d9e0ff0180953205cc18a950af6406a84748eea` | `fix/script-process-io-post131` |
| `9b3432e644ac44f497cff39c3a8d493c86e8b1cb` | `fix/vector-axis-order-single-normalization` |
| `9eecf75cfd46c1f3bb361571097c1670bb86c138` | `fix/vector-export-atomic-publication` |
| `0a62169c62eb238d366c7cba564095c5a800713e` | `fix/vector-reprojection-fail-closed-post145` |
| `faf1f46e2a9b5a545179b24f8aee939afc05ce03` | `fix/wfs-wcs-utf8-post-las-20261017` |
| `b6820731f4c49a1b6d521085099d734e2689db50` | `maintenance/ci-osgeo-dependency-resilience` |
| `8e9c1eadc321b8be39bc2e4d5627d35bd92bdb94` | `maintenance/github-storage-optimization` |
| `9bfb21bec61f7ab3c1b067240b4dfbe6620f6f6e` | `maintenance/gso-zero-success-artifacts` |
| `0222993846a1da055a1ce8a705277418b16f5e2c` | `maintenance/java-lf-integrity-post121` |
| `b994d0b7ab2e34a42b4d8f6e2b54047d1335bc26` | `port/202-specialized-layer-identity` |
| `3ac2e0c84dcf4ed67e0ef8a64435099e6d65193c` | `port/204-project-state-authority` |
| `d5b008ff2292b0902d303073d51299d1a6e05011` | `port/205-vector-source-crs` |
| `ff6fe3983736e03d8006720567239824c5c97529` | `quality/a0-critical-gate-evidence-20261017` |
| `e2461df91bfcac374fac20808e6c8332825f7b4d` | `quality/a0-external-tool-timeout-contract` |
| `70a9c55525a47bd87d5796805c0d217726cb77d0` | `quality/a0-gdal-executable-macos-linux-post63` |
| `2a45390b0f8f533708dc337398a351f4e8f8295d` | `quality/a0-gdal-xyz-atomic-publish-post71` |
| `1fa17199c7db6b50ff70e3ce929ded6f09d49b6e` | `quality/a0-gdal2tiles-command-timeout-post66` |
| `ff29cda6e7a3d66309712fe8a6de3d540afa6b03` | `quality/a0-gdal2tiles-real-xyz-roundtrip-post68` |
| `36cb0111c429c45475827f27f73e6f2b01d98a65` | `quality/a0-layer-boundary-ratchet` |
| `903704b41fae0f1820c94552a9668c775b367536` | `quality/a0-linux-gdal-native-path-post57` |
| `14480da02558e1141c1347b5d72f24c418477990` | `quality/a0-linux-jpackage-appimage-post59` |
| `6b6bf8ea78b26b992fa9576ece761f295a360d60` | `quality/a0-macos-intel-apple-silicon-appimage` |
| `50d9c67999401a4b6e827c3b227c775aec652475` | `quality/a0-map-panel-characterization-v2` |
| `33c9dde51ab73058d9ac84b0d0837700b7ad6ce6` | `quality/a0-online-cache-off-edt-consolidated` |
| `75b10e96ebb6ab3a15e463b1016687c37532ecf1` | `quality/a0-real-native-gdal-cli-macos-linux` |
| `b0b232cead23b9149e62222f71ce6945c98951a1` | `quality/a0-wcs-atomic-replay-post40` |
| `0842e4b220bf528e1083157521b898971f81f2d4` | `quality/a0-wcs-bounded-inmemory-post53` |
| `a3ddbbf1f620c5e83157206a94fd4be5bd1625ae` | `quality/a0-wcs-georeference-metadata-post54` |
| `580b92e9725361bf2f9331f97e86a7b99b7a83f3` | `quality/a0-wcs-tiff-guard-post51` |
| `d5f8b060f118a5e9b41e2782d2136d390098dcee` | `quality/a0-whitebox-edt-parameter-contracts` |
| `a30e59fe55adcb89a12487dd81f56dbaf19e7a16` | `quality/a0-whitebox-portable-executable-resolution` |
| `4fefffe9ecb392ad281e324be61bee95dc32682a` | `quality/a0-windows-appimage-contract-post61` |
| `30b69f4c1dc775504ef50443bd211d86a0379224` | `quality/a0-xyz-byte-budget-backing-and-concurrency` |
| `1da90fd0cf6e6407201f8741b4e9a78e37096fbb` | `quality/a0-xyz-cache-safety-consolidated-post41` |
| `a2afba21a17b74eacd38d02432b26b600fb82ff1` | `quality/a0-xyz-crash-recovery-journal-post75` |
| `74ad91077ef838dcb9421f351edc304973128c64` | `quality/a0-xyz-cross-process-lease-post72` |
| `6a1d96338ce0c36ff465c3d3ec4b8889d52733b7` | `quality/a0-xyz-persistent-cache-post55` |
| `4a2f49119281e2f006aa3ce1ccaa2aa92c97f57e` | `quality/a02-mappanel-zoom-pan-history-post77` |
| `85be3813a2b13ecfcbdd74efd53bbe960071eb24` | `quality/a02-pan-frame-preview-post83` |
| `a527a15af4fc4deb0cc8a3bff281152e2c87b41a` | `quality/a02-pure-pan-coordinate-deferral-post81` |
| `66e7ec5ed431a2ee900314c7f76baf4c01b0f6ee` | `quality/a02-real-geojson-pan-visual-contract-post84` |
| `cab79534f5c409019c124d2de68551ed4a1070db` | `quality/a03-catmap-atlas-isolated-post97` |
| `42ecb2f140def334fb441d2833b12f7e3247a591` | `quality/a03-catmap-page-aware-editor-post101` |
| `ca36829bb42a58bb1a774a210f07b219ab684313` | `quality/a03-wcs-dimensions-preflight-post105` |
| `ece11832fbb32ed14fd48d0aa544c15e7f03dcd3` | `quality/a03-wcs-raster-load-edt-post92` |
| `2c3e939047d5acfb16eeac6082941c25e4549925` | `quality/a03-wcs-spatial-preflight-post106` |
| `4d6fc158accc48a5acf606e1a55776c299026914` | `quality/a04-wcs-tiff-storage-post84` |
| `9321b6586d02c90b08a3a5eedb5393320b92903f` | `quality/a07-batch-output-plan-post111` |
| `540a56e599f5226b943fbedc51597884685b0408` | `quality/a07-batch-template-json-post119` |
| `322d174fcfa120d3303d498d138e34f126379753` | `quality/a07-catmap-atomic-export-raster-pdf-post103` |
| `4147d2b2fbf3978395ae89ddb33f99d7c875aa1b` | `quality/a07-catmap-atomic-layout-save-post93` |
| `90951f0a5d86ebc24ff16b7c8bfb135fdb711447` | `quality/a07-catmap-multipage-image-integrity-post100` |
| `b858ca88f833b828cf1fbcc350340cd149d0757d` | `quality/a07-catmap-pdf-multiline-post107` |
| `63647a3c8cdd8bf3cfe0d5e583cd0b7ca32bc8ed` | `quality/a07-catmap-raster-svg-atomic-post104` |
| `e2792652399fc1137fa7ea5142d14a5fa8b17418` | `quality/a07-coverage-audit-post115` |
| `34dd90e80517a18d8b516a01cbaba0b5186f460a` | `quality/a07-geoparquet-inspection-post118` |
| `8a5e27b79a12220267105dfe600e936668ad5f57` | `quality/a07-geoparquet-metadata-post116` |
| `63e19e54d9279dd856337e6c4ba1f084985ef786` | `quality/a07-wcs-byte-budget-post85` |
| `68cd8138533084ccd604d615a59bef0a64c82d6f` | `quality/a07-wcs-deadline-cancel-post90` |
| `eab902bde4b4b25a9125d8fe421c0bdb6f9f93e9` | `quality/a07-wcs-failclosed-atomic-post88` |
| `7165104f5b09dee6d06963ae536554d625107f53` | `quality/a07-wcs-interprocess-lease-post89` |
| `f218b61bd35749d0570b56643b917e541e7798be` | `quality/f1-dynamic-command-inventory` |
| `08d9fe79f689d439a1b4bc1ad1cf58ae64a9588b` | `quality/f1-semantic-feature-matrix` |
| `236ba2a910b0a0b1b97b9700fc62d05ff2eaa659` | `quality/f1-semantic-static-aliases` |
| `bf75a90f4af3715fae2aec8b723a0e8e32ca37db` | `quality/f1-source-inventory` |
| `9d1b824df27737739342b402938cedf92f43ba89` | `quality/f1-supplemental-semantic-mapping` |
| `c6972c719e59da6284c089efdaced5621f757c6d` | `quality/f1-surface-inventory` |
| `2e080e34093165c0efa274de22507b99ed7a83c7` | `quality/f1-ui-command-carrier-coverage-v2` |
| `55f56a4b1091472efaee745575d81d2ccc2ee6d5` | `quality/f1-visible-command-matrix` |
| `09b678bdd765b335c412b8bb490d8505b21c0846` | `quality/f1-visible-placeholder-reconciliation` |
| `8a8455472f813f700b4fa031eb3121c16453b246` | `quality/f2-catmap-golden-baseline` |
| `11a7753a9b55ab24caebbba00de1177bd092a7fb` | `quality/f2-core-project-contracts` |
| `62b5fcbe60840a8043062550e3b83151751b850c` | `quality/f2-grib-truthful-gating` |
| `984575e06ee9e883bee1e7538bb372c83fc61088` | `quality/f2-las-binary-contracts` |
| `8bcd6e41c01aba65aaf56d3eea57a5c80f7c8a5e` | `quality/f2-ogc-service-contracts` |
| `0d3ea514a18a3d9ba08871fb953db8a132b73130` | `quality/f2-postgis-pool-credentials` |
| `059ac99dff5e9fb409fef71bdec09d2f2cb6ba0a` | `quality/f2-raster-dem-contracts` |
| `8eb8d1cdd7f510ba4dcfad5665180df9adf1cedc` | `quality/f2-stac-pagination-integrity` |
| `43033f1a25e8c5d21d96091e4210e81205baa0c0` | `quality/f2-truthful-format-gating` |
| `3958fa56be4ea1272a4475259114fa9bef4e7729` | `quality/f2-vector-edit-contracts` |
| `19540e3942064fd11d886ff53e1fd669f1df0e13` | `quality/f2-wcs-namespace-secure-capabilities` |
| `bf75a90f4af3715fae2aec8b723a0e8e32ca37db` | `quality/g0-certification-harness` |
| `20a4e7df03f6a426bc7f967280422cac084e5645` | `quality/java-baseline-proof-post125` |
| `53d9ae4148b96ddbc1a2a339201f808a0e6ccde0` | `security/opentopography-diagnostics-post123` |
| `fc66a8323b6a3ac366263ca04dc4dbd4fb215007` | `spec/catgis-product-certification` |
| `5698ebbaf860d290c0c73bced6020e17a8f9430d` | `spec/github-architecture-first-certification` |
| `9ceef76736f5d935129e9e109329a8941889ad7f` | `test/terrain-dem-hermetic-smoke` |

## Ramas conservadas sin mergear (46)

No se tocaron: requieren triage por rama antes de cualquier borrado.

- `audit/fgb-crs-post-main-20261017`
- `docs/kiro-current-execution-post115`
- `fix/fgb-header-crs-verified-20261017`
- `fix/fgb-source-crs-truth-staging`
- `fix/fgb-unknown-crs-guard-20261017`
- `fix/las-header-integrity-containment`
- `fix/local-file-crs-after-wfs-20261017`
- `fix/project-live-state-post-ux-20261017`
- `fix/project-specialized-layer-codec-post136`
- `fix/project-state-authority`
- `fix/project-unsaved-appcontext-post-las-20261017`
- `fix/shp-geojson-kml-source-crs-roundtrip-20261017`
- `fix/source-crs-truth-fgb-las-post142`
- `fix/specialized-layer-payload-post-las-20261017`
- `fix/ui-source-utf8-literals-20261017`
- `fix/ux-mojibake-post160`
- `fix/vector-atomic-post-wfs-20261017`
- `fix/vector-single-file-atomic-post142`
- `fix/vector-singlefile-atomic-current-main-20261017`
- `fix/vector-singlefile-atomic-post-las-20261017`
- `fix/wfs-unknown-crs-current-main-20261017`
- `fix/wfs-unspecified-request-crs`
- `fix/wfs-wcs-utf8-current-main-20261017`
- `perf/p0-pan-lag`
- `quality/a0-gdal2tiles-autozoom-bounded-process`
- `quality/a0-linux-gdal-native-path-portability-post55`
- `quality/a0-main-push-certification-attestation`
- `quality/a0-map-render-state-characterization`
- `quality/a0-unix-gdal-explicit-native-bin`
- `quality/a0-wcs-atomic-stream-download`
- `quality/a0-wcs-bounded-pixel-integrity-post77`
- `quality/a0-wcs-tiff-structural-publication-guard`
- `quality/a0-wms-disk-cache-off-edt`
- `quality/a0-xyz-bounded-image-decoding`
- `quality/a0-xyz-portable-persistent-cache-root`
- `quality/a0-xyz-source-cache-isolation`
- `quality/a0-xyz-tile-cache-off-edt`
- `quality/a03-catmap-atlas-isolated-page-output`
- `quality/a04-wcs-tiff-pixel-integrity`
- `quality/attest-main-pr51-exact`
- `quality/attest-main-pr59`
- `quality/f1-ui-command-carrier-coverage`
- `quality/tool-approval-audit-post133`
- `research/geoparquet-rows-probe-post121`
- `test/wcs-deadline-cold-start`
- `test/wcs-shared-deadline-stable-20261017`

## Politica

- Toda rama nueva parte de `main` vivo y se certifica por PR exacto.
- Una rama mergeada se borra tras la atestacion post-main.
- Las ramas sin mergear solo se borran con triage documentado y aprobacion.
