#!/usr/bin/env bash
# Prepares a GitHub Release from a signed APK (REL-B, DEC-024):
#   prepare_release.sh <tag> <apk> <aapt2> <apksigner> <out-dir>
# - checks the tag (vX.Y.Z) matches the APK's versionName, and that the APK is signed
# - writes krantenwijk-X.Y.Z.apk, its .sha256, and release-notes.md into <out-dir>
# - prints the version name and code for the workflow log
# Release notes list the commits since the previous v* tag.
set -euo pipefail

tag="$1"; apk="$2"; aapt2="$3"; apksigner="$4"; out="$5"

if [[ ! "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "::error::Tag '$tag' is not of the form vX.Y.Z" >&2; exit 1
fi
version="${tag#v}"

badging="$("$aapt2" dump badging "$apk")"
apk_name="$(sed -nE "s/.*versionName='([^']+)'.*/\1/p" <<<"$badging" | head -1)"
apk_code="$(sed -nE "s/.*versionCode='([0-9]+)'.*/\1/p" <<<"$badging" | head -1)"
package="$(sed -nE "s/^package: name='([^']+)'.*/\1/p" <<<"$badging" | head -1)"

if [[ "$package" != "nl.ericmulder.krantenwijk" ]]; then
  echo "::error::Unexpected package '$package'" >&2; exit 1
fi
if [[ "$apk_name" != "$version" ]]; then
  echo "::error::Tag $tag does not match the app's versionName $apk_name. Bump versionName or fix the tag." >&2; exit 1
fi
if ! "$apksigner" verify "$apk" >/dev/null 2>&1; then
  echo "::error::The APK is not signed (release signing secrets missing?)" >&2; exit 1
fi

mkdir -p "$out"
asset="krantenwijk-$version.apk"
cp "$apk" "$out/$asset"
(cd "$out" && shasum -a 256 "$asset" > "$asset.sha256")

previous="$(git describe --tags --abbrev=0 --match 'v*' "$tag^" 2>/dev/null || true)"
{
  echo "Krantenwijk $version for Android 12 and newer."
  echo
  echo "**Install or update:** download \`$asset\` on your phone and open it. Your route and settings are kept."
  echo
  if [[ -n "$previous" ]]; then
    echo "### Changes since $previous"
    git log --no-merges --format='- %s' "$previous..$tag"
  else
    echo "### Changes"
    git log --no-merges --format='- %s' "$tag"
  fi
  echo
  echo "---"
  echo "versionCode: $apk_code"
  echo "sha256: $(cut -d' ' -f1 "$out/$asset.sha256")"
} > "$out/release-notes.md"

echo "version=$version"
echo "versionCode=$apk_code"
