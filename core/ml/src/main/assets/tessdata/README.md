# Tesseract trained data

**Not committed — obtain separately before building with OCR enabled.**

Place `eng.traineddata` in this folder:

    core/ml/src/main/assets/tessdata/eng.traineddata

Recommended source: **tessdata_fast** (Apache 2.0), ~2 MB — good enough for
printed receipt OCR with low APK-size impact.

```bash
curl -L -o core/ml/src/main/assets/tessdata/eng.traineddata \
    https://github.com/tesseract-ocr/tessdata_fast/raw/main/eng.traineddata
```

SHA-256 checksum for the `tessdata_fast` version at the time of v1.0:

    TO BE FILLED IN ON FIRST COMMIT:
    sha256sum core/ml/src/main/assets/tessdata/eng.traineddata

Verify the checksum before committing and bake the expected value into
`ReceiptOcr.kt` so the runtime can reject a tampered file.

If this file is absent:

- The APK builds fine.
- `ReceiptOcr.recognize()` returns null on first call.
- The Add-Transaction screen hides its "Scan receipt" button.

This mirrors the `core/ml/src/main/assets/model_v1.json` pattern — ML features
degrade gracefully to rule-based behavior when optional assets aren't bundled.
