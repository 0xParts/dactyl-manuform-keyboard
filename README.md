# Dactyl ManuForm Keyboard
This is a fork of <a href="https://github.com/tshort/dactyl-keyboard">The Dactyl-ManuForm Keyboard</a> which is a fork of <a href="https://github.com/adereth/dactyl-keyboard">The Dactyl Keyboard</a>.

This keyboard is a parameterized, split-hand, concave, columnar, ergonomic keyboard.

## Support
You can support this project further developement and our other keyboard related projects by visiting us at <a href="https://www.k33b.com">K33B.com</a> and purchasing a keyboard. Thank you.

## Features
- Change the tilt and curvature of the keyboard.
- You can enable / disable sidenubs for certain switches.
- Added option to use hot swap sockets.
- Outer column can be either 1.5u or 1u size keys.
- You can choose what row uses 1.5u.
- Toggle a extra outer column.
- Toggle a extra inner column before the thumb cluster with nrows-2.
- Toggle between four types of thumb clusters (default, <a href="https://github.com/l4u/dactyl-manuform-mini-keyboard">mini</a>, <a href="https://github.com/carbonfet/dactyl-manuform">cf</a>, <a href="https://github.com/okke-formsma/dactyl-manuform-tight">tightly</a>).
- Removable controller board holder for Raspberry Pi Pico with TRRS and USB-C cutouts.
- Tiltable hand rests with magnetic inserts.
- Bottom plate moved to fit inside the case, eliminating the border.

## Design generation
- Run `lein repl`
- Run this command `(def thumb-style "default")(def nrows 5)(def ncols 6)(load-file "src/dactyl.clj")`
- This will regenerate the `things/` files
- Use OpenSCAD to open the `.scad` files to view and/or export `.stl` files
- Alternatively use the `./run-generator.sh` script to generate all the designs
- Then you can use `./stl-export.sh` to generate all the `.stl` files


## License

Copyright © 2015-2023 Matthew Adereth, Tom Short, Leo Lou, Matija Golub.

The source code for generating the models is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).

The generated models are distributed under the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](LICENSE-models).
