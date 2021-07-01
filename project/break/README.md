# Project 'Break'

Advanced suite of generative tests targetting the CVM.

Builds upon [project 'Clojurify'] which provides utilities that allows to quickly write Convex Lisp as Clojure data and
evaluating such code for different scenarios. It also defines a series of useful [test.check]((https://github.com/clojure/test.check)
generators.

This great combination is showcased in all test files found in `./src/clj/test`. Those tests helped uncovering dozens of issues
and bugs in the CVM, contributing to making it strong and robust.

Those tests structures generative assertions using the [Mprop](https://github.com/helins/mprop.cljc) library. It is highly
recommended for writing sophisticated tests fearlessly.


## Running tests

See BB tasks at the root of this repository. By default, they take several minutes depending on hardware.


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
