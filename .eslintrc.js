module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
  },
  extends: ['airbnb', 'plugin:@typescript-eslint/recommended', 'plugin:react-native/all', 'prettier'],
  plugins: ['react', 'react-native', '@typescript-eslint'],
  env: {
    browser: true,
    jest: true,
    'react-native/react-native': true,
  },
  rules: {
    // General ESLint rules
    'no-unused-vars': 'error',
    'no-console': 'warn',
    'no-underscore-dangle': 'off',
    'import/no-unresolved': [2, { caseSensitive: false }],
    'import/extensions': [
      'error',
      'ignorePackages',
      {
        js: 'never',
        jsx: 'never',
        ts: 'never',
        tsx: 'never',
      },
    ],
    // React rules
    'react/jsx-filename-extension': ['error', { extensions: ['.jsx', '.tsx'] }],
    'react/jsx-props-no-spreading': 'off',

    // React Native rules
    'react-native/no-unused-styles': 'error',
    'react-native/split-platform-components': 'error',
    'react-native/no-inline-styles': 'error',
    'no-use-before-define': ['error', { variables: false }],

    // TypeScript rules
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/explicit-member-accessibility': 'off',
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
  },
};
