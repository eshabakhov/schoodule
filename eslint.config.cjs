const js = require('@eslint/js');
module.exports = [
    js.configs.recommended,
    {
        files: ['src/main/resources/static/js/**/*.js'],
        languageOptions: {
            ecmaVersion: 2023,
            sourceType: 'script',
            globals: {
                window: 'readonly',
                document: 'readonly',
                alert: 'readonly',
                confirm: 'readonly',
                setTimeout: 'readonly',
                clearTimeout: 'readonly',
                URLSearchParams: 'readonly',
                history: 'readonly',
                $: 'readonly',
                jQuery: 'readonly',
                initEntityCrud: 'readonly',
                initEditEntity: 'readonly',
                initSchoolCrud: 'readonly',
                initModals: 'readonly'
            }
        },
        rules: {
            'no-var': 'error',
            'prefer-const': 'error',
            'eqeqeq': ['error', 'always'],
            'no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
            'no-console': ['error', { allow: ['warn', 'error'] }]
        }
    }
];