Abstract
-

Solution uses producer consumer pattern where the main thread reads input sequentially via a file scanner and when a determined number of lines is reached the line data is dispatched to a consumer and the buffer is cleared. The consumer then attempts to match log entries within the batch recording any entries which cannot be matched to concurrent hashmap. When the consumer threads have finished the unmatched entries are then matched. The interaction with the database should then

I have prioritised a solution scalable to input size over other considerations due to time constraints as I think this is where the value lies. Memory usage is fixed due to constant batch size used across thread pool (aside from the unmatched items, see improvement 1)

Solution tested using a log generator written to create example text files in similar composition to the example given. 

Instructions
-
gradle run --args='filename.txt'

where filename is the desired input in resources folder


Improvements
-
I would propose the following given more time:
- When the unmatched items reach a certain size we could create a matching thread to match possible entries which keep the size of these structures within acceptable limit
- some minor design change to allow more abstraction, possible use of IoC container, and unit test coverage
- Improved exception management and edge case handling
- Add cmd line arguments to control threadpool size, dispatch size etc. further testing to finetune performance using said parameters

