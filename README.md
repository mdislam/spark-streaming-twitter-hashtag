spark-streaming-twitter-hashtag
===============================

Calculates popular hashtags (topics) over sliding 10 and 60 second windows from a Twitter  stream.

How to Run
==========

1. Open Terminal

2. Run ./bin/sbt.sh from project root directory

3. you will be in the scala command shell

4. run following commands
    > clean
    > package
    > run local[2] obama ALSIce


Please note that, I have run this only in local machine. Forgot to run in the ukko cluster. The arguments of run should be spark://ukkoXXX:7077 [filter1] [filter2] ... ...

