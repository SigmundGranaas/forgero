package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.material.MaterialManager;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class Utils {

    public static String createModelJson(String path, String parent) {
        String[] segments = path.split("/");
        path = segments[segments.length - 1];
        return "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"" + Forgero.MOD_NAMESPACE + ":item/" + path + "\"\n" +
                "  }\n" +
                "}";
    }

    public static String createModelJsonWithUniqueTexture(String texture) {
        return "{\n" +
                "  \"textures\": {\n" +
                "    \"0\": \"forgero:" + texture + "\",\n" +
                "    \"particle\": \"forgero:item/oak_binding\"\n" +
                "  },\n" +
                "  \"elements\": [\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_0\",\n" +
                "      \"from\": [\n" +
                "        9,\n" +
                "        6,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        9.5,\n" +
                "        11,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            7.5,\n" +
                "            12,\n" +
                "            2.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            2.5,\n" +
                "            12,\n" +
                "            7.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            2.5,\n" +
                "            12,\n" +
                "            7.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            2.5,\n" +
                "            12,\n" +
                "            7.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            2.5,\n" +
                "            11.5,\n" +
                "            3\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            7,\n" +
                "            12,\n" +
                "            7.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_1\",\n" +
                "      \"from\": [\n" +
                "        8.5,\n" +
                "        6.5,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        9,\n" +
                "        10.5,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            7,\n" +
                "            11.5,\n" +
                "            3\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            3,\n" +
                "            11.5,\n" +
                "            7\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            3,\n" +
                "            11.5,\n" +
                "            7\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            3,\n" +
                "            11.5,\n" +
                "            7\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            11.5,\n" +
                "            3,\n" +
                "            11,\n" +
                "            3.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            6.5,\n" +
                "            11.5,\n" +
                "            7\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_2\",\n" +
                "      \"from\": [\n" +
                "        9.5,\n" +
                "        7.5,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        10,\n" +
                "        10.5,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            6,\n" +
                "            12.5,\n" +
                "            3\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            3,\n" +
                "            12.5,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            3,\n" +
                "            12.5,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            3,\n" +
                "            12.5,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            12.5,\n" +
                "            3,\n" +
                "            12,\n" +
                "            3.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            12,\n" +
                "            5.5,\n" +
                "            12.5,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_3\",\n" +
                "      \"from\": [\n" +
                "        10,\n" +
                "        7.5,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        11.5,\n" +
                "        9,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            12.5,\n" +
                "            6,\n" +
                "            14,\n" +
                "            4.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            13.5,\n" +
                "            4.5,\n" +
                "            14,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            12.5,\n" +
                "            4.5,\n" +
                "            14,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            12.5,\n" +
                "            4.5,\n" +
                "            13,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            4.5,\n" +
                "            12.5,\n" +
                "            5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            12.5,\n" +
                "            5.5,\n" +
                "            14,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_4\",\n" +
                "      \"from\": [\n" +
                "        7,\n" +
                "        8,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        8.5,\n" +
                "        8.5,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            9.5,\n" +
                "            5.5,\n" +
                "            11,\n" +
                "            5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            5,\n" +
                "            11,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            9.5,\n" +
                "            5,\n" +
                "            11,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            9.5,\n" +
                "            5,\n" +
                "            10,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            5,\n" +
                "            9.5,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            9.5,\n" +
                "            5,\n" +
                "            11,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_5\",\n" +
                "      \"from\": [\n" +
                "        11.5,\n" +
                "        8,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        12,\n" +
                "        8.5,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            5.5,\n" +
                "            14.5,\n" +
                "            5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            5,\n" +
                "            14.5,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            5,\n" +
                "            14.5,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            5,\n" +
                "            14.5,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            14.5,\n" +
                "            5,\n" +
                "            14,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            14,\n" +
                "            5,\n" +
                "            14.5,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_6\",\n" +
                "      \"from\": [\n" +
                "        7.5,\n" +
                "        7.5,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        8.5,\n" +
                "        8,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            10,\n" +
                "            6,\n" +
                "            11,\n" +
                "            5.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            5.5,\n" +
                "            11,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            10,\n" +
                "            5.5,\n" +
                "            11,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            10,\n" +
                "            5.5,\n" +
                "            10.5,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            5.5,\n" +
                "            10,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            10,\n" +
                "            5.5,\n" +
                "            11,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"iron_binding_7\",\n" +
                "      \"from\": [\n" +
                "        8,\n" +
                "        7,\n" +
                "        7.3\n" +
                "      ],\n" +
                "      \"to\": [\n" +
                "        8.5,\n" +
                "        7.5,\n" +
                "        8.7\n" +
                "      ],\n" +
                "      \"rotation\": {\n" +
                "        \"angle\": 0,\n" +
                "        \"axis\": \"y\",\n" +
                "        \"origin\": [\n" +
                "          10,\n" +
                "          10,\n" +
                "          8\n" +
                "        ]\n" +
                "      },\n" +
                "      \"faces\": {\n" +
                "        \"north\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            6.5,\n" +
                "            11,\n" +
                "            6\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"east\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            6,\n" +
                "            11,\n" +
                "            6.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"south\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            6,\n" +
                "            11,\n" +
                "            6.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"west\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            6,\n" +
                "            11,\n" +
                "            6.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"up\": {\n" +
                "          \"uv\": [\n" +
                "            11,\n" +
                "            6,\n" +
                "            10.5,\n" +
                "            6.5\n" +
                "          ],\n" +
                "          \"rotation\": 180,\n" +
                "          \"texture\": \"#0\"\n" +
                "        },\n" +
                "        \"down\": {\n" +
                "          \"uv\": [\n" +
                "            10.5,\n" +
                "            6,\n" +
                "            11,\n" +
                "            6.5\n" +
                "          ],\n" +
                "          \"texture\": \"#0\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gui_light\": \"front\",\n" +
                "  \"display\": {\n" +
                "    \"thirdperson_righthand\": {\n" +
                "      \"rotation\": [\n" +
                "        50,\n" +
                "        5,\n" +
                "        90\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0.25,\n" +
                "        2.5,\n" +
                "        3\n" +
                "      ],\n" +
                "      \"scale\": [\n" +
                "        0.85,\n" +
                "        0.85,\n" +
                "        0.85\n" +
                "      ]\n" +
                "    },\n" +
                "    \"thirdperson_lefthand\": {\n" +
                "      \"rotation\": [\n" +
                "        60,\n" +
                "        0,\n" +
                "        -90\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0,\n" +
                "        2.5,\n" +
                "        3\n" +
                "      ],\n" +
                "      \"scale\": [\n" +
                "        0.85,\n" +
                "        0.85,\n" +
                "        0.85\n" +
                "      ]\n" +
                "    },\n" +
                "    \"firstperson_righthand\": {\n" +
                "      \"rotation\": [\n" +
                "        114,\n" +
                "        -9,\n" +
                "        -90\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0.5,\n" +
                "        5,\n" +
                "        1.75\n" +
                "      ],\n" +
                "      \"scale\": [\n" +
                "        0.68,\n" +
                "        0.68,\n" +
                "        0.68\n" +
                "      ]\n" +
                "    },\n" +
                "    \"firstperson_lefthand\": {\n" +
                "      \"rotation\": [\n" +
                "        -66,\n" +
                "        -168,\n" +
                "        -95\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        -2.25,\n" +
                "        5,\n" +
                "        1.75\n" +
                "      ],\n" +
                "      \"scale\": [\n" +
                "        0.68,\n" +
                "        0.68,\n" +
                "        0.68\n" +
                "      ]\n" +
                "    },\n" +
                "    \"ground\": {\n" +
                "      \"rotation\": [\n" +
                "        88,\n" +
                "        2,\n" +
                "        0\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0,\n" +
                "        0.25,\n" +
                "        0\n" +
                "      ],\n" +
                "      \"scale\": [\n" +
                "        0.5,\n" +
                "        0.5,\n" +
                "        0.5\n" +
                "      ]\n" +
                "    },\n" +
                "    \"gui\": {\n" +
                "      \"rotation\": [\n" +
                "        0,\n" +
                "        -90,\n" +
                "        90\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0,\n" +
                "        0,\n" +
                "        -0.75\n" +
                "      ]\n" +
                "    },\n" +
                "    \"head\": {\n" +
                "      \"translation\": [\n" +
                "        0,\n" +
                "        7,\n" +
                "        -0.25\n" +
                "      ]\n" +
                "    },\n" +
                "    \"fixed\": {\n" +
                "      \"rotation\": [\n" +
                "        0,\n" +
                "        91,\n" +
                "        90\n" +
                "      ],\n" +
                "      \"translation\": [\n" +
                "        0,\n" +
                "        0,\n" +
                "        -0.75\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"groups\": [\n" +
                "    {\n" +
                "      \"name\": \"iron_binding\",\n" +
                "      \"origin\": [\n" +
                "        8,\n" +
                "        8,\n" +
                "        8\n" +
                "      ],\n" +
                "      \"color\": 0,\n" +
                "      \"children\": [\n" +
                "        0,\n" +
                "        1,\n" +
                "        2,\n" +
                "        3,\n" +
                "        4,\n" +
                "        5,\n" +
                "        6,\n" +
                "        7\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @Nullable
    public static InputStream readJsonResourceAsString(String path) {
        return MaterialManager.class.getResourceAsStream(path);
    }
}
