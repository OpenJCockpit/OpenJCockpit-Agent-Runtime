package nl.metafactory.agentruntime.definition.litellm;

import nl.metafactory.agentruntime.definition.model.DefinitionSource;
import nl.metafactory.common.agentdefinition.SourceStatus;
import nl.metafactory.agentruntime.definition.skill.SkillPackageProvider;

public class ExternalSkillPackageProvider implements SkillPackageProvider {

    public static final String REASON =
            "LiteLLM v1.101.2 exposes no skill content/archive API (/v1/skills returns metadata only)";

    @Override
    public DefinitionSource source() {
        return DefinitionSource.REMOTE_LITELLM;
    }

    @Override
    public SkillLookup lookup(String skillId) {
        return SkillLookup.of(SourceStatus.UNSUPPORTED, REASON);
    }
}
