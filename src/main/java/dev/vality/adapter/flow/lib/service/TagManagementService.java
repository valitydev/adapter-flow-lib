package dev.vality.adapter.flow.lib.service;

import java.util.Map;

public interface TagManagementService {

    String findTag(Map<String, String> parameters);

    String initTag(String tagId);
}
