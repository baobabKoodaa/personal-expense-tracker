package baobab.pet.data.domain;

import javax.persistence.*;

@Entity
public class Category {

    private long id;
    private long groupId; /* determines which categories are available to which books. */
    private String name;

    public Category() {
        super();
    }

    public Category(String name, long groupId) {
        this();
        this.name = name;
        this.groupId = groupId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String[] bahCategorySplitted() {
        String raw = this.getName();
        String[] out = new String[3];
        out[0] = "";
        out[1] = "";
        out[2] = "";
        StringBuilder sb = new StringBuilder();
        int j = 0;
        for (int i=0; i<raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '/' && j < 2) {
                out[j] = sb.toString();
                sb = new StringBuilder();
                j++;
            } else {
                sb.append(c);
            }
        }
        out[j] = sb.toString();
        return out;
    }
}
