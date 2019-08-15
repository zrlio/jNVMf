<!--
{% comment %}

Copyright (C) 2018, IBM Corporation

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
{% endcomment %}
-->

# jNVMf: A NVMe over Fabrics library for Java
jNVMf is a Java library for remote access to NVMe storage. It implements client side of the NVMe over Fabrics (NVMf) specification. jNVMf has been tested to work with [SPDK](http://www.spdk.io), [Linux](http://www.kernel.org) and [Mellanox offloading](https://community.mellanox.com/docs/DOC-2918) targets. Futhermore it supports advanced NVMe/f features like fused operations, incapsule data and dataset management.

## Building jNVMf

Building the source requires [Apache Maven](http://maven.apache.org/) and a Java JDK with Java version 8.
jNVMf depends on [DiSNI](http://www.github.com/zrlio/disni) while the java dependecy is automatically picked up from maven central you need to download and compile the native library of DiSNI. Check the DiSNI README for instructions on how to build the native library. To build jNVMf use:

``mvn -DskipTests package`` or ``mvn -DskipTests install``

## Run Unit tests

To run the unit tests make sure the LD_LIBRARY_PATH contains libdisni (see above) and execute:

``mvn test -Prdma -DargLine="-DdestinationAddress=10.100.0.5 -DlocalAddress=10.100.0.2 -Dnqn=nqn.2017-06.io.crail:cnode4420 -Dport=4420"``

where ``destinationAddress`` is the target IP address, ``localAddress`` is the local IP of the RDMA device, ``nqn`` the NVMe qualified name of the controller you want to connect to and ``port`` the port of the target.

## Run benchmark

jNVMf provides a small benchmark tool. Run:

``java -cp target/jnvmf-1.7-jar-with-dependencies.jar:target/jnvmf-1.7-tests.jar com.ibm.jnvmf.utils.NvmfClientBenchmark 
--help``

to shows the available arguments. 

For example:

``java -cp target/jnvmf-1.7-jar-with-dependencies.jar:target/jnvmf-1.7-tests.jar com.ibm.jnvmf.utils.NvmfClientBenchmark -a 10.100.0.22 -p 4420 -g 4096 -i 3 -m RANDOM -n 10 -nqn nqn.2016-06.io.spdk:cnode1 -o bench.csv -qd 1 -rw read -s 4096 -qs 64 -H -I``

* executes a ``-m RANDOM`` ``-rw read`` test
* to a target at ``-a 10.100.0.22``, ``-p 4420`` with controller ``-nqn nqn.2016-06.io.spdk:cnode1``
* all accesses are aligned to ``-g 4096`` bytes
* statistics are printed every ``-i 3`` seconds ``-n 10`` times
* output is written to ``-o bench.csv`` in csv format
* queue depth ``-qd 1``
* transfer size ``-s 4096`` bytes
* queue size ``-qs 64`` entries
* histogram ``-H``
* RDMA inline data ``-I``

## Run diagnostics

jNVMf offers a tool to run diagnostics. Print help with:

``java -cp target/jnvmf-1.7-jar-with-dependencies.jar:target/jnvmf-1.7-tests.jar com.ibm.jnvmf.utils.NvmfDiagnostics 
  --help``
  
Example:

``java -Djnvmf.legacy=true -cp jnvmf-1.7-jar-with-dependencies.jar:jnvmf-1.7-tests.jar com.ibm.jnvmf.utils.NvmfDiagnostics -a 10.100.0.22 -p 4420 -nqn nqn.2016-06.io.spdk:cnode1 -t asyncevent``

* executes ``-t asyncevent`` test
* to a target at ``-a 10.100.0.22``, ``-p 4420`` with controller ``-nqn nqn.2016-06.io.spdk:cnode1``

## Example API usage

Sample usage of the jNVMf API to perform a read operation:

```
Nvme nvme = new Nvme();
/* target ip and port we want to connect to */
InetSocketAddress socketAddress = new InetSocketAddress("10.100.0.1", 4420);
/* controller NVMe qualified name */
NvmfTransportId tid = new NvmfTransportId(socketAddress,
                new NvmeQualifiedName("nqn.2016-06.io.spdk:cnode1"));
/* connect to controller */
Controller controller = nvme.connect(tid);
/* enable controller */
controller.getControllerConfiguration().setEnable(true);
controller.syncConfiguration();
controller.waitUntilReady();
/* create a io queue pair with submission queue size 64 */
IoQueuePair queuePair = controller.createIoQueuePair(64);
/* allocate and register buffer to be used for transfer */
ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
KeyedNativeBuffer registeredBuffer = queuePair.registerMemory(buffer);
/* create a new read command */
NvmReadCommand command = new NvmReadCommand(queuePair);
/* create a response */
Response<NvmResponseCapsule> response = command.newResponse();
/* set buffer */
NvmIOCommandCapsule commandCapsule = command.getCommandCapsule();
commandCapsule.setSglDescriptor(buffer);
/* set length, LBA and namespace identifier */
NvmIoCommandSqe sqe = commandCapsule.getSubmissionQueueEntry();
sqe.setStartingLba(0);
/* read one block*/
sqe.setNumberOfLogicalBlocks(1);
sqe.setNamespaceIdentifier(new NamespaceIdentifier(1));
volatile boolean done = false;
/* set callback */
command.setCallback(new OperationCallback() {
      @Override
      public void onStart() {
      }

      @Override
      public void onComplete() {
          System.out.println("command completed");
          /* check response */
          NvmCompletionQueueEntry cqe = response.getResponseCapsule().getCompletionQueueEntry();
          StatusCode.Value statusCode = cqe.getStatusCode();
          if (!statusCode.equals(GenericStatusCode.getInstance().SUCCESS)) {
              System.out.println("Command not successful");
          }
          done = true;
      }

      @Override
      public void onFailure(RdmaException e) {
          System.out.println("There was an RDMA error when executing the command");
          done = true;
      }
});
command.execute(response);
/* wait until command is done */
while (!done);
```

## Contributions

PRs are always welcome. Please fork, and make necessary modifications, and let us know. 

## Contact 

If you have questions or suggestions, feel free to post at:

https://groups.google.com/forum/#!forum/zrlio-users

or email: zrlio-users@googlegroups.com
