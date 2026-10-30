#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <arm64-app-path> <x86_64-app-path> <output-universal-app-path>"
    exit 1
fi

ARM_APP="$1"
X64_APP="$2"
OUT_APP="$3"

if [ ! -d "$ARM_APP" ]; then
    echo "Error: arm64 app directory not found: $ARM_APP"
    exit 1
fi

if [ ! -d "$X64_APP" ]; then
    echo "Error: x86_64 app directory not found: $X64_APP"
    exit 1
fi

rm -rf "$OUT_APP"
mkdir -p "$(dirname "$OUT_APP")"
cp -R "$ARM_APP" "$OUT_APP"

echo "Merging Mach-O binaries into Universal application..."
find "$ARM_APP" -type f | while read -r arm_file; do
    if [ -L "$arm_file" ]; then
        continue
    fi
    rel_path="${arm_file#$ARM_APP/}"
    x64_file="$X64_APP/$rel_path"
    univ_file="$OUT_APP/$rel_path"

    if [ -f "$x64_file" ] && [ ! -L "$x64_file" ]; then
        if file -b "$arm_file" | grep -q "Mach-O" && file -b "$x64_file" | grep -q "Mach-O"; then
            arm_archs=$(lipo -archs "$arm_file" 2>/dev/null || echo "")
            x64_archs=$(lipo -archs "$x64_file" 2>/dev/null || echo "")
            if [ -n "$arm_archs" ] && [ -n "$x64_archs" ] && [ "$arm_archs" != "$x64_archs" ]; then
                echo "Merging Mach-O: $rel_path ($arm_archs + $x64_archs)"
                lipo -create "$arm_file" "$x64_file" -output "$univ_file"
            fi
        fi
    fi
done

if [ -f "$OUT_APP/Contents/MacOS/ReadYou" ]; then
    chmod +x "$OUT_APP/Contents/MacOS/ReadYou"
fi

if command -v codesign >/dev/null 2>&1 && [ -f "$OUT_APP/Contents/Info.plist" ]; then
    echo "Applying ad-hoc code signature to Universal bundle..."
    codesign --force --deep --sign "${MACOS_SIGN_IDENTITY:--}" "$OUT_APP"
fi

echo "Successfully created Universal macOS app: $OUT_APP"
