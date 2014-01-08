#EZcheck#

EZcheck is a simple program for checking links. It is specifically designed to be used by libraries to check catalog links from a MARC 856 field. However,
EZcheck can be used to check links from any source. It accepts input in the form of delimited text and .MRC MARC record files. It can also connect directly to a Sierra database using
Sierra's Direct SQL Access interface, provided you have a username that has the appropriate permissions in your Sierra system.

EZcheck is designed to check huge amounts of links at once--potentialyl millions. EZcheck works by using regular expressions to find URLs in a field of text.
It makes a lightweight HTTP HEAD request to a URL and reports the HTTP Status Code received from the remote resource. EZcheck utilizes multithreading so that multiple links can
be checked at once. If a single link takes a long time to respond, the entire program will not stall, as other threads are able to continue making requests.

Copyright 2014
Josh Welker, University of Central Missouri
GPL 2.0 License