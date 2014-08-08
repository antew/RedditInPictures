package com.antew.redditinpictures.library.model.reddit;

import java.util.List;

public class MoreComments {
    public List<Child> things;

    public MoreComments(List<Child> things) {
        this.things = things;
    }

    public List<Child> getThings() {
        return things;
    }

    public void setThings(List<Child> things) {
        this.things = things;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoreComments that = (MoreComments) o;

        if (things != null ? !things.equals(that.things) : that.things != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return things != null ? things.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MoreComments{" +
                "things=" + things +
                '}';
    }
}
