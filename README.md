### What is mongeez?

mongeez allows you to manage changes of your mongo documents and propagate these changes in sync with your code changes when you perform deployments.

This is a fork of [mongeez](https://github.com/mongeez/mongeez/) with support of mongo 4.2+, even after removal of support of
$eval by mongo. More details [here](https://www.mongodb.com/docs/v4.2/release-notes/4.2-compatibility/#remove-support-for-the-eval-command).

This is possible now with running the mongo js scripts indirectly from java application via the mongo
shell [load command](https://www.mongodb.com/docs/v4.4/reference/method/load). To run, it requires mongo shell client to
be installed and available in the path to the java executable. 

For further information and usage guidelines check out [the wiki](https://github.com/mongeez/mongeez/wiki/How-to-use-mongeez).

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
