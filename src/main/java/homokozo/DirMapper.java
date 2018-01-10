package homokozo;

import java.io.File;
import java.util.*;

public class DirMapper {

    public static void main(String[] args) {
        Map<String, Object> map = mapDirs(args[0]);
        System.out.println(map);
    }

    public static Map<String, Object> mapDirs(String path) {
        File startDir = new File(path);
        if (!startDir.isFile() && !startDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Can't open path %s, exiting...", path));
        } else if (startDir.isFile()) {
            throw new IllegalArgumentException(String.format("Path %s is file, exiting....", path));
        }
        return mapDirsWorker(startDir);
    }

    private static Map<String, Object> mapDirsWorker(File currentDir) {
        Map<String, Object> dirMap = new LinkedHashMap<>();
        if (currentDir.isDirectory() && currentDir.canRead()) {
            File[] currentSubDirs = currentDir.listFiles();
            if (currentSubDirs != null && currentSubDirs.length > 0) {
                for (File file : currentSubDirs) {
                    Map<String, Object> subDirMap;
                    if (file.isDirectory()) {
                        subDirMap = mapDirsWorker(file);
                        if (subDirMap.size() > 0) {
                            dirMap.put(file.getName(), subDirMap);
                        } else {
                            dirMap.put(file.getName(), null);
                        }
                    } else if (file.isFile()) {
                        dirMap.put(file.getName(), new File(file.getName()));
                    }
                }
            }
        }
        return dirMap;
    }

    public class TreeNode {

        final String name;
        final List<TreeNode> children;

        public TreeNode(String name, List<TreeNode> children) {
            this.name = name;
            this.children = children;
        }

        public void print() {
            print("", true);
        }

        private void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + name);
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
            }
            if (children.size() > 0) {
                children.get(children.size() - 1)
                    .print(prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }
}
