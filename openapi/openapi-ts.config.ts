import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'openapi.yml',
    output: {
        path: 'src',
        postProcess: ['oxfmt', 'oxlint'],
        entryFile: true,
        clean: true,
        module: {
            extension: 'js'
        }
    },
    logs: {
        file: true,
        path: "logs"
    },
    plugins: [
        {
            name: "@tanstack/react-query",
        },
        {
            name: "@hey-api/client-fetch",
        },
        {
            name: "@hey-api/typescript",
        }
    ],
});