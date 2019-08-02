<span style="backround-color: #AAAAAA">
    <b>NOTE:</b> Under active development.
</span>

# hugit

The `hu`mane Terminal UI for `git`!

The general philosophy is:
* **Live** tracks changes in the repo, and updates the UI automatically
* **Help** all available actions and their keybindings are displayed at all times

## Installation

## Features

### Files

- [x] stage files
- [x] unstage files
- [x] checkout files
- [x] untrack files

### Commits

- [x] show current head
- [x] commit with message
- [ ] show unmerged commits
- [ ] ammend
- [ ] rebase

### Branches

- [x] show current branch
- [ ] checkout branch

## Thanks

* [magit]()
* [Cljs TUI Template](https://github.com/eccentric-j/cljs-tui-template) and all its dependencies
* [nodegit]()

## Development

**Install**

- [nvm]()
- [node]() `9.11.1`

**Compile**

- `npm start` will start the compiler and watch for changes and recompile automatically
- `node --inspect target/js/compiled/maggit.js` to run the app. It will reflect changes automatically.

**Issues**

- install `nodegit` via `yarn` if `npm` complains

### 

## License
Copyright 2019 Divyansh Prakash, Manisha Pillai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this project except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
