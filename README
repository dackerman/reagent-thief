# Thief Game in Reagent

This is a little game I hacked together during a weekend to learn Reagent and
practice Clojurescript.  It's pretty janky and slow, doesn't keep score, and
generally is only mildly fun to play (but it was fun to create!).  Feel free
to do whatever you want with this code, it's MIT licensed.

It's based on the iOS game [Thief](https://itunes.apple.com/us/app/amazing-thief/id914079393?mt=8)
which my friends and I got quite addicted to.  It's a really simple game for
how much we ended up playing it, so I wanted to try to see how quickly I could
build it.

### The game in action
[The game in action](reagent-thief.gif)

## How to run

1. Get a relatively recent version of Java
1. Get leiningen
1. Run `lein cljsbuild once` to generate the javascript
1. Run `lein uberjar` to create a self-contained jar with frontend and backend
1. Run `java -jar ./target/thief-0.1.0-SNAPSHOT-standalone.jar` - this will open up a Clojure repl
1. In the repl, type `(run)` to start the server at port 10555 or pass in your own port, i.e. `(run 9000)`

## How to play

* After running, load the game at `http://localhost:10555` or whatever port you chose
* Click on the game to focus it (so key events are detected)
* Press "d" to jump. You can jump once while on the ground, and once while in the air (double-jump).
Make sure you time it right, sometimes you have to save your double-jump for the right moment or
else you'll lose enough height and fall through the cracks!


## The License
    The MIT License (MIT)

    Copyright (c) 2015 David Ackerman

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
