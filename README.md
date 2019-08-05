<span> 
    <b>NOTE:</b> Under active development.
</span>

# hugit

The `hu`mane Terminal UI for `git`!

The general philosophy is:
* **Live** tracks changes in the repo, and updates the UI automatically
* **Help** all available actions and their keybindings are displayed at all times
* **Logs** are always accessible

## Installation

## Features

### Files

- [x] stage files
- [x] unstage files
- [x] checkout files
- [x] untrack files

### Hunks

- [x] stage hunks
- [x] unstage hunks
- [ ] discard hunks

### Commits

- [x] show commit log
- [x] show commit
- [x] show current head
- [x] commit with message
- [ ] show unmerged (unpushed) commits
- [ ] ammend
- [ ] rebase

### Branches

- [x] show current branch
- [ ] checkout branch

### Remote

- [x] Push to `origin HEAD` (upstream branch with the same name)
- [ ] Change remote
- [ ] Push to custom branch

## Install

* Install [nvm](https://github.com/nvm-sh/nvm)
* `git clone https://github.com/divs1210/hugit.git`
* `cd hugit`
* `nvm use 9.11.1`
* `npm install`
* `npx shadow-cljs release app`

## Run

`cd` to a git repo, and run:
* `nvm use 9.11.1`
* `node path/to/hugit/target/main.js`

## Develop

**Compile**

* `cd path/to/hugit`
* `nvm use 9.11.1`
* `npm start`

This will:
  * start the compiler and watch for changes and recompile automatically
  * start a Clojure nrepl, connecting to which and running `(repl)` will start a ClojureScript repl

**Run**

* `cd path/to/hugit`
* `nvm use 9.11.1`
* `node --inspect target/js/compiled/hugit.js` to run the app

Changes to the `hugit` source should automatically reflect in the UI.
`hugit` is used to develop `hugit` whenever possible!
We will keep at it till we never have to use anything else.

**Issues**

* Install `nodegit` via `yarn` if `npm` complains: `yarn add nodegit@0.24.3`

## Contributors

* [Manisha Pillai](https://github.com/Manisha38)
* [Divyansh Prakash](https://github.com/divs1210)

## Thanks

* [Cljs TUI Template](https://github.com/eccentric-j/cljs-tui-template)
* [nodegit](https://github.com/nodegit/nodegit)

## License
Copyright 2019 Manisha Pillai, Divyansh Prakash

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this project except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
