package xyz.e3ndr.athena.transcoding;

import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;

public class CommandBuilder {
    private List<String> args = new LinkedList<>();

    public CommandBuilder add(@NonNull String... args) {
        for (String a : args) {
            this.args.add(a);
        }
        return this;
    }

    public CommandBuilder add(@NonNull List<String> args) {
        this.args.addAll(args);
        return this;
    }

    public List<String> asList() {
        return this.args;
    }

}
