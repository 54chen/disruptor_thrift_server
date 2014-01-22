/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.thinkaurelius.thrift.util;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransportFactory;

public class ThriftFactories
{
    public final TTransportFactory inputTransportFactory, outputTransportFactory;
    public final TProtocolFactory  inputProtocolFactory,  outputProtocolFactory;
    public final TProcessorFactory processorFactory;
    public final int maxFrameSizeInBytes;

    public ThriftFactories(TTransportFactory inputTransportFactory, TTransportFactory outputTransportFactory,
                           TProtocolFactory  inputProtocolFactory,  TProtocolFactory outputProtocolFactory,
                           TProcessorFactory processorFactory, int maxFrameSizeInBytes)
    {
        this.inputTransportFactory = inputTransportFactory;
        this.outputTransportFactory = outputTransportFactory;
        this.inputProtocolFactory = inputProtocolFactory;
        this.outputProtocolFactory = outputProtocolFactory;
        this.processorFactory = processorFactory;
        this.maxFrameSizeInBytes = maxFrameSizeInBytes;
    }
}