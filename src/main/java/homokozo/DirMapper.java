package homokozo;

import java.io.File;
import java.util.*;

public class DirMapper {

    public static void main(String[] args) {
        Map<String, Object> map = null;
        try {
            map = mapDirs(args[0]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println(map);
        TreeNode node = treeify(map, args[0]);
        node.print();
    }

    private static Map<String, Object> mapDirs(String path) throws IllegalArgumentException {
        File startDir = new File(path);
        if (!startDir.isFile() && !startDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Can't open path %s, exiting...", path));
        } else if (startDir.isFile()) {
            throw new IllegalArgumentException(String.format("Path %s is file, exiting...", path));
        }
        return mapDirsWorker(startDir);
    }

    private static Map<String, Object> mapDirsWorker(File currentDir) {
        Map<String, Object> dirMap = new LinkedHashMap<>();
        if (currentDir.isDirectory() && currentDir.canRead()) {
            File[] currentSubDirs = currentDir.listFiles();
            if (currentSubDirs != null && currentSubDirs.length > 0) {
                Map<String, Object> subDirMap;
                for (File file : currentSubDirs) {
                    String sanitizedName = file.getName();
                    if (file.isDirectory()) {
                        subDirMap = mapDirsWorker(file);
                        dirMap.put(sanitizedName, !subDirMap.isEmpty() ? subDirMap : null);
                    } else if (file.isFile()) {
                        dirMap.put(sanitizedName, sanitizedName);
                    }
                }
            }
        }
        return dirMap;
    }

    private static TreeNode treeify(Map<String, Object> map, String rootDir) {
        return new TreeNode(rootDir, treeifyChildren(map));
    }

    private static List<TreeNode> treeifyChildren(Map<String, Object> map) {
        List<TreeNode> children = new ArrayList<>();
        Set<Map.Entry<String, Object>> values = map.entrySet();
        for (Map.Entry<String, Object> o : values) {
            Object value = o.getValue();
            if (value != null && !(value instanceof String)) {
                children.add(new TreeNode(o.getKey(), treeifyChildren(((Map) value))));
            } else if (value != null) {
                children.add(new TreeNode(value.toString(), new ArrayList<>()));
            } else {
                children.add(new TreeNode(o.getKey(), new ArrayList<>()));
            }
        }
        return children;
    }

    private static String sanitize(String input) {
        return input.replaceAll("[\\r\\n]", "");
    }

    // respectfully stolen from SO user VasyaNovikov

    static class TreeNode {

        final String name;
        final List<TreeNode> children;

        TreeNode(String name, List<TreeNode> children) {
            this.name = sanitize(name);
            this.children = children;
        }

        void print() {
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
