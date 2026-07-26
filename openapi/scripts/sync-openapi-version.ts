import { parse } from "yaml";

const specFile = Bun.file("./openapi.yml");
const spec = parse(await specFile.text());

const pkgFile = Bun.file("./package.json");
const pkg = await pkgFile.json();

pkg.version = spec.info.version;

await Bun.write("./package.json", JSON.stringify(pkg, null, 2) + "\n");

console.log(`Synced package.json version to ${spec.info.version}`);