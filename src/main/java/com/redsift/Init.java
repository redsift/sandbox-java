package com.redsift;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Init {
    public static ObjectMapper mapper = new ObjectMapper();
    public String[] nodes;
    public String SIFT_ROOT;
    public String SIFT_JSON;
    public String IPC_ROOT;
    public Boolean DRY;

    public Init(String args[]) throws Exception {
        if (args.length <= 0) {
            throw new Exception("No nodes to execute");
        }

        String SIFT_ROOT = System.getenv("SIFT_ROOT");
        String SIFT_JSON = System.getenv("SIFT_JSON");
        String IPC_ROOT = System.getenv("IPC_ROOT");
        Boolean DRY = System.getenv("DRY") == "true";

        if (SIFT_ROOT == null || SIFT_ROOT == "") {
            throw new Exception("Environment SIFT_ROOT not set");
        }

        File file = new File(SIFT_ROOT);
        if (!file.isAbsolute()) {
            throw new Exception("Environment SIFT_ROOT '" + SIFT_ROOT + "' must be absolute");
        }

        if (SIFT_JSON == null || SIFT_JSON == "") {
            throw new Exception("Environment SIFT_JSON not set");
        }

        if (IPC_ROOT == null || IPC_ROOT == "") {
            throw new Exception("Environment IPC_ROOT not set");
        }

        if (DRY) {
            System.out.println("Unit Test Mode");
        }

        this.nodes = args;
        this.SIFT_ROOT = SIFT_ROOT;
        this.SIFT_JSON = SIFT_JSON;
        this.IPC_ROOT = IPC_ROOT;
        this.DRY = DRY;
    }
}
/*

var sift = JSON.parse(fs.readFileSync(path.join(SIFT_ROOT, SIFT_JSON), 'utf8'));

if ((sift.dag === undefined) || (sift.dag.nodes === undefined)) {
    throw new Error('Sift does not contain any nodes');
}

module.exports = {
  nodes: nodes,
  SIFT_ROOT: SIFT_ROOT,
  SIFT_JSON: SIFT_JSON,
  IPC_ROOT: IPC_ROOT,
  DRY: DRY,
  sift: sift
};
*/
