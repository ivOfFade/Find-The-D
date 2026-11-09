# Java libraries

The project uses the JARs in this folder directly, without a build framework.
Use `lib/*` on the compile and run classpath. In VS Code, add `lib/*.jar` to
**Java Projects → Referenced Libraries** if imports are unresolved.

- jBCrypt: existing password hashing dependency.
- Open Location Code: existing local Plus Code validation/decoding/encoding.
- PostgreSQL JDBC: existing database driver.
- [Gson 2.11.0](https://github.com/google/gson/releases/tag/gson-parent-2.11.0):
  parses the JSON returned by Overpass. It is a JSON library, not Google Maps;
  it requires no Google account or API key. The Apache 2.0 license is in
  `gson-LICENSE.txt`.
