Release notes
=========================

?????????????????????????
-------------------------
- upgrade to btrplace-solver 0.39
- bug fixes

1.4 - 19 Feb 2014
-------------------------
- upgrade to btrplace 0.35
- fix some typos in the api and the documentation

version 1.3 - 26 Nov 2013
-------------------------
- upgrade to btrplace 0.34
- better integration with existing models

version 1.2 - 21 Jun 2013
-------------------------
- update dependencies to BtrPlace-0.30
- simplify the code

version 1.1 - 17 Feb 2013
-------------------------
- Migrate the code from entropy to btrplace:solver-api
- operator '>>' before a constraint to declare a discrete restriction only
- JSON export for the scripts

version 0.106 - 9 oct. 2012
-------------------------
- bug fixes
- finer error messages
- the error reporting system can be embedded.

version 0.104 - 12 sep. 2012
-------------------------
- Prettier type reporting in error messages
- update dependencies

version 0.103 - 6 sep. 2012
-------------------------
- support for the 'online' and the 'offline' constraints
- better error reporting

version 0.102 - 21 feb. 2012
-------------------------
- Bug fix in range parsing
- Bug fix on resource matching when working in pair with Entropy
- Access to script dependencies
- Change cli btrp to btrplint

version 0.101 - 14 nov. 2011
-------------------------
- String support
- Float number support
- Bug fix in the import cache
- Bug fix in set division or multiplication
- Improve error reporting
- Variables and expressions can now be used within a range of elements
- export statement is now regular. No more shortcuts:
  Use 'export foo to *' to export to anyone

version 0.100 - 18 oct. 2011
-------------------------
- Documentation fixes
- The immutable variable to designate all the VMs declared in the script.
- 'this' has been removed. Replaced by '$me'
- Improve error reporting


version 0.99 - 8 oct. 2011
-------------------------
- code quality
- improve tutorial
- string or number as options value
- the available platform for the nodes can be declared similar to VM
  templates

version 0.98 - 31 aug. 2011
-------------------------
- Template validation when asked
- Some constraints now transform a single element into a singleton
  to simplify their declaration
- Minor bug fixes
- Compatibility with the last snapshot of Entropy-2.1
- The export statement can be refined to limit the namespaces that have an
  access to the variables
- Move the 'btrp' executable to the root of the distribution
- New tutorial

version 0.97 - 25 aug. 2011
-------------------------
- protobuf output format for the vjobs
- btrpsl depends now on entropy-2.1-SNAPSHOT. A lot of code is now already in
  entropy
- A VM option can now be either a single word or a key/value pair

version 0.96 - 6 aug. 2011
-------------------------
- XML output format for the vjobs
- minor bug fix in the sample scripts
- reduce memory footprint and runtime
- minor bug fixes

version 0.95 - 31 jul. 2011
-------------------------
- java style wildcard to import namespaces
- automatic creation of variables denoting the VMs in the imported namespaces
- a LRU cache for BtrPlaceVJobBuilder to avoid to read a same file multiple
  time
- move to antlr 3.4

version 0.94 - 18 jul. 2011
-------------------------
- Improve error messages
- bug fix
- Starting a tutorial that present examples provided in the distribution

version 0.93 - 17 jul. 2011
-------------------------
- Improve error messages
- bug fixing
- a command line tool to execute scripts. Just prints the resulting vjob on
  success

version 0.92 - 14 jul. 2011
-------------------------
- Parametric types for templates
- bug fix and API documentation

version 0.91 - 13 jul. 2011
-------------------------
- Implementation of VM template
- API documentation and tests

version 0.9 - 12 jul. 2011
-------------------------
- Namespace is now required
- Every VM identifier is automatically prefixed by the namespace to prevent
  naming conflict.

version 0.8 - 4 jul. 2011
-------------------------
- Remove a bug in if statements. Forgot to make the context switch
- Fix a bug in assignment that wasn't copying the value
- Implements '&&' and '||' operator
- some tests and documentation
- remove duplication bugs due to set composed of variables.

version 0.76 - 3 jul. 2011
-------------------------
- tiny bug fixes in FenceBuilder, AmongBuilder
- some tests and API documentation

version 0.75 - 3 jul. 2011
-------------------------
- better recognition of domain name (allows '-' characters)
- namespace delimiter becomes '.' instead of '/'. Fully aligned with variables

version 0.7 - 26 jun. 2011
-------------------------
- _import_ keyword instead of _use_
- definition of a namespace for the vjob
- cannot export variables when the vjob does not belong to a namespace
- _export_ allows a sequence of variables
- API documentation
- several bug fixes

version 0.6 - 25 jun 2011
-------------------------
- cardinality operator changed for |_set_| to _#set_. Much more convenient
- _export_ takes as a parameter either a variable or a set of variables
- API documentation
- bug fixed in the operands

Coming next
========================
- permissive constraint catalog
- bug fixes && documentation


