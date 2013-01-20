
package net.cattaka.droiball.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.GenDbHandler;

@GenDbHandler(find = {
        "id", "name", ":sort", ":sort-"
}, unique = {
        "name", "sort"
})
public class ActionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Attribute(primaryKey = true)
    private Long id;

    private Long sort;

    private String name;

    @Attribute(persistent = false)
    private List<PoseModel> poseModels;

    public ActionModel() {
    }

    public void set(ActionModel src) {
        this.id = src.id;
        this.sort = src.sort;
        this.name = src.name;
        if (src.poseModels != null) {
            this.poseModels = new ArrayList<PoseModel>();
            for (PoseModel model : src.poseModels) {
                this.poseModels.add(new PoseModel(model));
            }
        } else {
            this.poseModels = null;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PoseModel> getPoseModels() {
        return poseModels;
    }

    public void setPoseModels(List<PoseModel> poseModels) {
        this.poseModels = poseModels;
    }

}
