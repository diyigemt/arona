function transformVoicedOrSemiVoiced(char: string): string {
  const voiced = new RegExp(
    /[がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポ]/g
  );
  const semiVoiced = new RegExp(/[ぱぴぷぺぽパピプペポ]/g);

  return char
    .replace(
      voiced,
      match =>
        String.fromCharCode(match.charCodeAt(0) - 1) +
        String.fromCharCode(0x3099)
    )
    .replace(
      semiVoiced,
      match =>
        String.fromCharCode(match.charCodeAt(0) - 1) +
        String.fromCharCode(0x309a)
    );
}

export { transformVoicedOrSemiVoiced };
