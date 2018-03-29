# jNVMf: A NVMe over Fabrics library for Java
jNVMf is a Java library for remote access to NVMe storage. It implements client side of the NVMe over Fabrics (NVMf) specification. jNVMf has been tested to work with [SPDK](http://www.spdk.io), [Linux](http://www.kernel.org) and [Mellanox offloading](https://community.mellanox.com/docs/DOC-2918) targets. Futhermore it supports advanced NVMe/f features like fused operations, incapsule data and dataset management.

## Building jNVMf

Building the source requires [Apache Maven](http://maven.apache.org/) and a Java JDK with Java version 8 or higher.
jNVMf depends on [DiSNI](http://www.github.com/zrlio/disni) while the java dependecy is automatically picked up from maven central you need to download and compile the native library of DiSNI. Check the DiSNI README for instructions on how to build the native library. To build jNVMf use:

``mvn -DskipTests package`` or ``mvn -DskipTests install``

## Run Unit tests

To run the unit tests make sure the LD_LIBRARY_PATH contains libdisni (see above) and execute:

``mvn test -Prdma -DargLine="-DdestinationAddress=10.100.0.5 -DlocalAddress=10.100.0.2 -Dnqn=nqn.2017-06.io.crail:cnode4420 -Dport=4420"``

where ``destinationAddress`` is the target IP address, ``localAddress`` is the local IP of the RDMA device, ``nqn`` the NVMe qualified name of the controller you want to connect to and ``port`` the port of the target.

## Run benchmark

jNVMf provides a small benchmark tool which can be run with:

``java -cp target/jnvmf-1.0-jar-with-dependencies.jar:target/jnvmf-1.0-tests.jar com.ibm.jnvmf.benchmark.NvmfClientBenchmark 
--help``

which shows the available arguments. For example:

``java -cp target/jnvmf-1.0-jar-with-dependencies.jar:target/jnvmf-1.0-tests.jar com.ibm.jnvmf.benchmark.NvmfClientBenchmark -a 10.100.0.22 -p 4420 -g 4096 -i 3 -m RANDOM -n 10 -nqn nqn.2017-06.io.crail:cnode4420 -o bench.csv -qd 1 -rw read -s 4096 -qs 64 -H -I``

executes a ``-m RANDOM`` ``-rw read`` test to a target at ``-a 10.100.0.22``, ``-p 4420`` with controller ``nqn nqn.2017-06.io.crail:cnode4420``, all accesses are aligned to ``-g 4096`` bytes, statistics are printed every ``-i 3`` seconds ``-n 10`` times, output is written to ``-o bench.csv`` in csv format, queue depth ``-qd 1``, transfer size ``-s 4096`` bytes, queue size ``-qs 64`` entries, histogram ``-H`` and RDMA inline data ``-I``

## Contributions

PRs are always welcome. Please fork, and make necessary modifications, and let us know. 

## Contact 

If you have questions or suggestions, feel free to post at:

https://groups.google.com/forum/#!forum/zrlio-users

or email: zrlio-users@googlegroups.com
