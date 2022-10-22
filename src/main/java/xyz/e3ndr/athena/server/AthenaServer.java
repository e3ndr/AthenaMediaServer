package xyz.e3ndr.athena.server;

import java.io.IOException;

import xyz.e3ndr.athena.Config;

public interface AthenaServer {

    public void start(Config config) throws IOException;

}
