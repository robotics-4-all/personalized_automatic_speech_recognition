package org.pasr.prep.email;


import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;


public class EmailTree extends TreeView<String> {
    public void add(Folder folder){
        String[] folders = folder.getPath().split("/");
        int numberOfFolders = folders.length;

        EmailTreeItem newFolder = EmailTreeItem.createFolder(folders[numberOfFolders - 1]);
        for(Email email : folder.getEmails()){
            newFolder.getChildren().add(EmailTreeItem.createEmail(email.getSubject(), email.getBody()));
        }

        int depth = 0;
        TreeItem<String> currentFolder = getRoot();
        while(depth < numberOfFolders){
            TreeItem<String> existingSubFolder = containsAsFolder(currentFolder, folders[depth]);
            if (existingSubFolder != null){
                currentFolder = existingSubFolder;
                depth++;

                if(depth == numberOfFolders - 1){
                    currentFolder.getChildren().add(newFolder);
                    break;
                }
            }
            else{
                break;
            }
        }
        if(depth < numberOfFolders - 1 || depth == 0){
            for(int i = depth, n = numberOfFolders - 1;i < n;i++){
                EmailTreeItem parentFolder = EmailTreeItem.createFolder(folders[i]);
                currentFolder.getChildren().add(parentFolder);

                currentFolder = parentFolder;
            }

            currentFolder.getChildren().add(newFolder);
        }
    }

    private TreeItem<String> containsAsFolder(TreeItem<String> item, String value){
        for(TreeItem<String> child : item.getChildren()){
            if(child.getValue().equals(value) &&
                ((EmailTreeItem)(child)).isFolder()){
                return child;
            }
        }

        return null;
    }

}
