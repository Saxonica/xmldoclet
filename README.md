# xmldoclet

This is a Javadoc doclet that produces XML output. The goal is that
all of the data available in the Doclet API should be available in the
resulting XML.

It’s still a little rough around the edges, but it does use the modern
JavaDoc APIs and not the old, now deprecated ones that appear in
several other doclets for this purpose.

## Installing the doclet

You have two choices for installing xmldoclet. You can download a release
from the Github [releases](https://github.com/Saxonica/xmldoclet/releases) page,
or you can use the Saxonica Maven repository at `https://dev.saxonica.com/maven`.
The Maven artifact is:

```xml
<dependency>
    <groupId>com.saxonica</groupId>
    <artifactId>xmldoclet</artifactId>
    <version>x.y.z</version>
</dependency>
```

Where `x.y.z` should be replaced by the appropriate version.

Make sure that the xmldoclet jar file is on the classpath used by the Javadoc
command.

## Running the doclet

The xmldoclet recognizes the following options:

<dl>
<div>
<dt><code>-d</code></dt>
<dd>The destination directory, if specified this is where the output file
will be stored. By default, this is <code>.</code>, the current directory.
</dd>
</div>
<div>
<dt><code>-outputfile</code></dt>
<dd>The name of the output file. By default, this is <code>doclet.xml</code>.
</dd>
</div>
<div>
<dt><code>-sourcepath</code></dt>
<dd>A list of directories where Java source files can be found. See <em>source paths</em>
below.
</dd>
</div>
<div>
<dt><code>-doctitle</code>, <code>-windowtitle</code>, <code>-notimestamp</code></dt>
<dd>These options are accepted, but ignored. This allows the doclet to be
used with build tools that send them without having to work out how to
suppress them.
</dd>
</div>
</dt>

### Source paths

The new Javadoc APIs don’t appear to expose any record of imported
classes. You can’t tell, for example, that `org.example.MyClass`
contains an `import java.util.Locale` statement. Inspecting the source
code for the standard doclet, reveals that this information is
accessed through a variety of private APIs (the information is *in*
the objects, but not exposed in any public APIs!).

Unfortunately, *without* access to the imports, it’s not possible to
tell what an unqualified type name refers to! What is _Locale
mylocale_? It could be `java.util.Local` or it could be
`org.example.otherpackage.Locale`.

The xmldoclet applies brute force. It relies on the `javaparser` APIs
and simply reads the sources itself. That’s why you have to specify a
source path. This can lead to spurious warnings from the Java parser,
but they aren’t relevant because the only thing xmldoclet uses the
parsed AST for is to extract the list of imports.

## XML output

The XML output is in the `https://saxonica.com/ns/doclet` namespace.
It conforms to the RELAX NG schema `doclet.rnc` in
`xmldoclet/src/main/resources/doclet.rnc`.

All of the comments and tags are in the HTML namespace, wrapped in a
`body` element. They are guaranteed to be well-formed, even if the
markup in the Java sources is not. This is achieved by parsing them
with the Validator.nu HTML 5 parser.

Notes:

* The HTML 5 parser doesn’t preserve comments. To work around this
  limitation, any literal comments in the Javadoc source:
  
  ```xml
  <!-- this is a comment. -->
  ```
  
  are represented using an HTML extension element:
  `xmldoclet-comment`. For example:

  ```xml
  <xmldoclet-comment text=" this is a comment. "/>
  ```

* Some `char` constant values can’t be represented in XML. For
  surrogate pairs and characters less than 0x20, the string
  `(char) 0x…` is used for the value instead of the character’s
  literal value.

## Feedback welcome

Much of the output is the result of experimentation and exploring the
Javadoc APIs. If you discover that xmldoclet produces output that
doesn’t conform to the schema, or if you think that the output is
incomplete or incorrect, please [open an issue](https://github.com/Saxonica/xmldoclet/issues).

## Change log

* **0.15.0** Show nested classes and interfaces

    As a convenience, the list of `classref` and `interfaceref` elements in a package
    now includes nested classes and interfaces.

* **0.14.0** Fixed package name

    The package name was sometimes (e.g., in the superclass type)
    incorrect (missed in the fix to [#10](https://github.com/Saxonica/xmldoclet/issues/10)).

* **0.13.0** Handle type parameters on methods, renamed a few attributes

    Extended support for type parameters to methods. On several elements, renamed
    the `type` attribute to `name` for consistency with `fullname`. Updated the
    schema to be correct wrt the current output.

* **0.12.0** Fix type equality comparison, fix parameterized type purposes

    Fixed the issue where the purposes for all params were output for each
    type parameter (fixed [#18](https://github.com/Saxonica/xmldoclet/issues/18)).
    Replaced the awful type equality hack with a proper comparison.
    (fixed [#16](https://github.com/Saxonica/xmldoclet/issues/16)).

* **0.11.0** Improve type equality comparison when looking for overrides

  The solution here is a fairly awful hack. Will have to come back to this and try to do better.

* **0.10.0** Fix superclass interfaces; output information about type parameters

  Moved the interface information about a superclass into the superclass
  (fixed [#13](https://github.com/Saxonica/xmldoclet/issues/13)).
  Output JavaDoc descriptions associated with parameters in parameterized classes
  (fixed [#14](https://github.com/Saxonica/xmldoclet/issues/14)).

* **0.9.0** Fixed package and type names; fixed interface lists

  Package and type names are correct (fixed [#10](https://github.com/Saxonica/xmldoclet/issues/10)),
  and the interfaces of supertypes are included in the list of implemented interfaces
  (fixed [#11](https://github.com/Saxonica/xmldoclet/issues/11)).

* **0.8.0** Fixed method names

  Output the “simple” method name in the name attribute on method elements.
  The full signature is also provided and the parameters and their types are available
  from children.

* **0.7.0** Improved presentation of interfaces

  Reworked the way interfaces are presented so that the methods inherited
  from those interfaces (i.e., the methods not actually implemented on this class)
  are shown.

* **0.6.0** Improved handling of method names and inheritance
  
  Changed the “name” of methods to include the parameter types. (i.e., `foo(int)`
  instead of `foo`). This allows them to be distinguished.
  
  Fixed a bug where the methods inherited from interfaces were not shown.
  
  Fixed a bug in computing visibility. Previously, the code looked for an explicit
  ~public~ or ~protected~ modifier. Now it *excludes* things explicitly marked ~private~.
  
  Experimentally removed ~java.lang.Object~ as a supertype in the generated XML.

The previous version looked for methods and fields marked as public or protected, but that's not the same thing!

* **0.5.0** String constants now use “backslash-U” escapes for non-ASCII characters.
