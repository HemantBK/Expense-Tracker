// Conventional Commits — enforced on every commit via lefthook.
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'docs', 'test', 'build', 'ci', 'perf', 'chore', 'revert', 'style'],
    ],
    'header-max-length': [2, 'always', 100],
    'body-max-line-length': [2, 'always', 120],
    'subject-case': [2, 'always', ['sentence-case', 'lower-case']],
  },
};
