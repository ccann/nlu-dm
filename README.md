Dynamic Initiative Dialog Operator -- ADE component
See manuscript.pdf

BUILDING:
./ant slab

RUNNING:
./ant run-registry -Df="config/dido.config" | grep -v .*meeting.*
// the grep is to remove an annoying "error" that prints to STDOUT constantly

./ant run-registry -Df="config/dido.config" | grep .*DIDO.*
// or do this in order to just see dido's output

NOTES:
- there is no navigation going on and the component only knows the location of the breakroom. So, when it "drives to
the breakroom" it is only driving forward. If you move the robot and it is still close enough to the breakroom to
initiative the move, it will just drive forward, it will not navigate to the breakroom.

- Typing in sentences on the fly will not work. The semantics are pre-canned because I couldn't get NLP to generate them
well enough. See "CanningComponentImpl".



