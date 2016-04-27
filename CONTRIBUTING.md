#Contributing to BtrPlace

BtrPlace is an Open source project. Feel free to contribute

## Using the issue tracker

The [tracker](https://github.com/btrplace/scheduler/issues) is the central place to report problems but also to state
intentions. Each modification (bug fix, enhancement, new feature, ...) or intention must be reported using the issue
tracker. Ideally before acting.

If you have the rights to do so, flag your issue to a milestone and label it.
This eases a lot the creation of the changelog.

## Reporting bugs

Well, we test as much as we can, but it happens that some modification or corner cases and not properly covered.
If you think you hit a bug:

1. Search in the [tracker](https://github.com/btrplace/scheduler/issues) to see if the bug has already been reported
or fixed (do not forget to look for closed issues).

1. Isolate the problem, describe it and provide a [Minimal Working Example](https://en.wikipedia.org/wiki/Minimal_Working_Example).
If possible, try to reproduce the bug on the *master* branch and do not forget to indicate which version was used to
reproduce the bug.

Doing so, we will endeavor to reproduce the bug and fix it as soon as possible in the master branch.
If the bug is critical, a release could be done in advance.

### Reporting bad scheduling decisions

If you only rely on legacy Btrplace components, it is straighforward to save a problem inputs using the JSON
serialisation format. Import the `scheduler-json` artifact and serialize the inputs as follow:

```
Model mo = ...
List<SatConstraint> cstrs = ...
OptConstraint opt = ...

Instance i = new Instance(mo, cstrs, opt);
InstanceConverter ic = new InstanceConverter()
String json = ic.toJSONString(i);
```

In the bug report:

1. provide the `json` string and the scheduler configuration parameters.
2. state what is expected, and what you observed.

## Feature requests

Feature requests are welcome, we always appreciate having feedback.
For your ideas to be considered, please give us as much details as possible, some practical cases are bonus.

