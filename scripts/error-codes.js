import * as properties from 'java-properties';
import * as glob from 'glob';
import * as fs from "node:fs/promises";

const errorCodesProperties = properties.of('../framework/src/main/resources/org/fulib/fx/lang/error.properties');
const errorCodesMd = await fs.readFile('../ERROR_CODES.md', 'utf-8');

const errorCodes = {};
for (const property of errorCodesProperties.getKeys()) {
    const code = +property;
    if (isNaN(code)) {
        continue;
    }
    const message = errorCodesProperties.get(code).replace(/%s/g, '*');
    errorCodes[code] = {
        message,
        runtime: false,
        annotationProcessor: false,
    };
}

await scanCodes('../framework/src/main/java', 'runtime');
await scanCodes('../annotation-processor/src/main/java', 'annotationProcessor');

const newErrorCodesMd = errorCodesMd.replace(/^### (\d+).*(?:\r?\n)+- Runtime: ..?\r?\n- Annotation Processor: ..?/gm, (match, code) => {
    const error = errorCodes[code];
    if (error) {
        error.matched = true;
        return `### ${code}: \`${error.message}\`

- Runtime: ${error.runtime ? '✅' : '❌'}
- Annotation Processor: ${error.annotationProcessor ? '✅' : '❌'}`;
    }
    return match;
});

await fs.writeFile('../ERROR_CODES.md', newErrorCodesMd);

for (const error of Object.values(errorCodes)) {
    if (!error.matched) {
        console.warn(`Error code ${error} not found in ERROR_CODES.md`);
    }
}

async function scanCodes(folder, property) {
    for await (const file of glob.globIterate(`${folder}/**/*.java`)) {
        const content = await fs.readFile(file, 'utf-8');
        for (const match of content.matchAll(/error\((\d+)\)/g)) {
            const code = +match[1];
            if (errorCodes[code]) {
                errorCodes[code][property] = true;
            }
        }
    }
}
