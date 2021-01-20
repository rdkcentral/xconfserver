/*******************************************************************************
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *******************************************************************************/
package com.comcast.xconf.permissions;

import java.util.Objects;

public final class EntityPermission {

    private String readAll;

    private String readStb;

    private String readXhome;

    private String readRdkcloud;

    private String readSky;

    private String writeAll;

    private String writeStb;

    private String writeXhome;

    private String writeRdkcloud;

    private String writeSky;

    private EntityPermission(Builder builder) {
        if (builder.readAll == null || builder.readStb == null || builder.readXhome == null || builder.readRdkcloud == null || builder.readSky == null
                || builder.writeAll == null || builder.writeStb == null || builder.writeXhome == null || builder.writeRdkcloud == null || builder.writeSky == null) {
            throw new IllegalArgumentException("Any field should not be null");
        }

        this.readAll = builder.readAll;
        this.readStb = builder.readStb;
        this.readXhome = builder.readXhome;
        this.readRdkcloud = builder.readRdkcloud;
        this.readSky = builder.readSky;

        this.writeAll = builder.writeAll;
        this.writeStb = builder.writeStb;
        this.writeXhome = builder.writeXhome;
        this.writeRdkcloud = builder.writeRdkcloud;
        this.writeSky = builder.writeSky;
    }

    public String getReadAll() {
        return readAll;
    }

    public String getReadStb() {
        return readStb;
    }

    public String getReadXhome() {
        return readXhome;
    }

    public String getReadRdkcloud() {
        return readRdkcloud;
    }

    public String getReadSky() {
        return readSky;
    }

    public String getWriteAll() {
        return writeAll;
    }

    public String getWriteStb() {
        return writeStb;
    }

    public String getWriteXhome() {
        return writeXhome;
    }

    public String getWriteRdkcloud() {
        return writeRdkcloud;
    }

    public String getWriteSky() {
        return writeSky;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityPermission{");
        sb.append("readAll='").append(readAll).append('\'');
        sb.append(", readStb='").append(readStb).append('\'');
        sb.append(", readXhome='").append(readXhome).append('\'');
        sb.append(", readRdkcloud='").append(readRdkcloud).append('\'');
        sb.append(", readSky='").append(readSky).append('\'');
        sb.append(", writeAll='").append(writeAll).append('\'');
        sb.append(", writeStb='").append(writeStb).append('\'');
        sb.append(", writeXhome='").append(writeXhome).append('\'');
        sb.append(", writeRdkcloud='").append(writeRdkcloud).append('\'');
        sb.append(", writeSky='").append(writeSky).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityPermission that = (EntityPermission) o;
        return Objects.equals(readAll, that.readAll) &&
                Objects.equals(readStb, that.readStb) &&
                Objects.equals(readXhome, that.readXhome) &&
                Objects.equals(readRdkcloud, that.readRdkcloud) &&
                Objects.equals(readSky, that.readSky) &&
                Objects.equals(writeAll, that.writeAll) &&
                Objects.equals(writeStb, that.writeStb) &&
                Objects.equals(writeXhome, that.writeXhome) &&
                Objects.equals(writeRdkcloud, that.writeRdkcloud) &&
                Objects.equals(writeSky, that.writeSky);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readAll, readStb, readXhome, readRdkcloud, readSky, writeAll, writeStb, writeXhome, writeRdkcloud, writeSky);
    }

    public static class Builder {
        private String readAll;

        private String readStb;

        private String readXhome;

        private String readRdkcloud;

        private String readSky;

        private String writeAll;

        private String writeStb;

        private String writeXhome;

        private String writeRdkcloud;

        private String writeSky;

        public Builder() {}

        public Builder(String readAll, String readStb, String readXhome, String readRdkcloud, String readSky, String writeAll, String writeStb, String writeXhome, String writeRdkcloud, String writeSky) {

            this.readAll = readAll;
            this.readStb = readStb;
            this.readXhome = readXhome;
            this.readRdkcloud = readRdkcloud;
            this.readSky = readSky;
            this.writeAll = writeAll;
            this.writeStb = writeStb;
            this.writeXhome = writeXhome;
            this.writeRdkcloud = writeRdkcloud;
            this.writeSky = writeSky;
        }

        public Builder setReadAll(String readAll) {
            this.readAll = readAll;
            return this;
        }

        public Builder setReadStb(String readStb) {
            this.readStb = readStb;
            return this;
        }

        public Builder setReadXhome(String readXhome) {
            this.readXhome = readXhome;
            return this;
        }

        public Builder setReadRdkcloud(String readRdkcloud) {
            this.readRdkcloud = readRdkcloud;
            return this;
        }

        public Builder setReadSky(String readSky) {
            this.readSky = readSky;
            return this;
        }

        public Builder setWriteAll(String writeAll) {
            this.writeAll = writeAll;
            return this;
        }

        public Builder setWriteStb(String writeStb) {
            this.writeStb = writeStb;
            return this;
        }

        public Builder setWriteXhome(String writeXhome) {
            this.writeXhome = writeXhome;
            return this;
        }

        public Builder setWriteRdkcloud(String writeRdkcloud) {
            this.writeRdkcloud = writeRdkcloud;
            return this;
        }

        public Builder setWriteSky(String writeSky) {
            this.writeSky = writeSky;
            return this;
        }

        public EntityPermission build() {
            return new EntityPermission(this);
        }
    }
}
