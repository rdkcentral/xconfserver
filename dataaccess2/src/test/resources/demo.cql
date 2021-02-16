/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE demo;

CREATE TABLE "ChangedKeys" (key bigint, column1 timeuuid, value VARCHAR, PRIMARY KEY (key, column1)) WITH default_time_to_live = 604800;
CREATE TABLE "SimpleDaoCF" (key VARCHAR PRIMARY KEY, column1 text, value VARCHAR) WITH COMPACT STORAGE;
CREATE TABLE "ListingDaoCF" (key VARCHAR, lastname VARCHAR, value VARCHAR, PRIMARY KEY (key, lastname)) WITH COMPACT STORAGE;
CREATE TABLE "ListingCompressedDaoCF" (key VARCHAR, lastname VARCHAR, value VARCHAR, PRIMARY KEY (key, lastname)) WITH COMPACT STORAGE;
CREATE TABLE "Users_ACL" (key bigint PRIMARY KEY, column1 text, value VARCHAR) WITH COMPACT STORAGE;

CREATE TABLE "NameSpacedList2" ( key text, column1 blob, value blob, PRIMARY KEY (key, column1) ) WITH COMPACT STORAGE;