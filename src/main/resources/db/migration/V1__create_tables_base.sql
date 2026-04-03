CREATE TABLE usuarios (
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    cpf              VARCHAR(14)  NOT NULL UNIQUE,
    senha            VARCHAR(255) NOT NULL,
    cep              VARCHAR(9)   NOT NULL,
    cidade           VARCHAR(255) NOT NULL,
    bairro           VARCHAR(255) NOT NULL,
    rua              VARCHAR(255) NOT NULL,
    num_endereco     BIGINT       NOT NULL,
    telefone         VARCHAR(20),
    perfil           VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE autoridades (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE autoridades_usuario (
    id_usuario    BIGINT NOT NULL REFERENCES usuarios(id),
    id_autoridade BIGINT NOT NULL REFERENCES autoridades(id),
    PRIMARY KEY (id_usuario, id_autoridade)
);

CREATE TABLE animais (
    id            BIGSERIAL    PRIMARY KEY,
    nome          VARCHAR(255) NOT NULL,
    especie       VARCHAR(20)  NOT NULL,
    idade         BIGINT       NOT NULL,
    genero        VARCHAR(20)  NOT NULL,
    porte         VARCHAR(20)  NOT NULL,
    descricao     TEXT         NOT NULL,
    disponivel    BOOLEAN      NOT NULL DEFAULT TRUE,
    vacinado      BOOLEAN      NOT NULL DEFAULT FALSE,
    castrado      BOOLEAN      NOT NULL DEFAULT FALSE,
    microchipado  BOOLEAN      NOT NULL DEFAULT FALSE,
    id_responsavel BIGINT      REFERENCES usuarios(id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE animal_midias (
    id             BIGSERIAL    PRIMARY KEY,
    animal_id      BIGINT       NOT NULL REFERENCES animais(id),
    url            VARCHAR(500) NOT NULL,
    chave_s3       VARCHAR(500) NOT NULL,
    tipo           VARCHAR(10)  NOT NULL,
    principal      BOOLEAN      NOT NULL DEFAULT FALSE,
    ordem          INTEGER      NOT NULL DEFAULT 0,
    tamanho_bytes  BIGINT       NOT NULL,
    mime_type      VARCHAR(100) NOT NULL,
    nome_original  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE pedidos_adocao (
    id                        BIGSERIAL   PRIMARY KEY,
    data_pedido               DATE        NOT NULL,
    status                    VARCHAR(20) NOT NULL,
    observacoes               TEXT        NOT NULL,
    score_matching            INTEGER,
    id_animal                 BIGINT      REFERENCES animais(id),
    id_adotante               BIGINT      REFERENCES usuarios(id),
    id_voluntario_responsavel BIGINT      REFERENCES usuarios(id),
    created_at                TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE doacoes (
    id         BIGSERIAL      PRIMARY KEY,
    data       TIMESTAMP      NOT NULL,
    valor      NUMERIC(10,2)  NOT NULL,
    categoria  VARCHAR(20)    NOT NULL,
    descricao  TEXT,
    id_doador  BIGINT         REFERENCES usuarios(id),
    id_animal  BIGINT         REFERENCES animais(id),
    created_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE perfis_adotante (
    id                   BIGSERIAL    PRIMARY KEY,
    id_usuario           BIGINT       NOT NULL UNIQUE REFERENCES usuarios(id),
    tipo_moradia         VARCHAR(30)  NOT NULL,
    area_m2              INTEGER,
    tem_outros_pets      BOOLEAN      NOT NULL,
    descricao_outros_pets TEXT,
    rotina_diaria        TEXT         NOT NULL,
    horas_em_casa_por_dia INTEGER     NOT NULL,
    renda_mensal_faixa   VARCHAR(20)  NOT NULL,
    experiencia_animais  BOOLEAN      NOT NULL,
    motivacao_adocao     TEXT         NOT NULL,
    especie_preferida    VARCHAR(20),
    porte_preferido      VARCHAR(20),
    aceita_criancas      BOOLEAN      NOT NULL,
    score_risco          INTEGER,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE despesas (
    id             BIGSERIAL     PRIMARY KEY,
    descricao      VARCHAR(255)  NOT NULL,
    valor          NUMERIC(10,2) NOT NULL,
    data           DATE          NOT NULL,
    categoria      VARCHAR(20)   NOT NULL,
    observacoes    TEXT,
    id_animal      BIGINT        REFERENCES animais(id),
    id_responsavel BIGINT        REFERENCES usuarios(id),
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE itens_estoque (
    id                 BIGSERIAL     PRIMARY KEY,
    nome               VARCHAR(255)  NOT NULL,
    categoria          VARCHAR(20)   NOT NULL,
    unidade_medida     VARCHAR(20)   NOT NULL,
    quantidade_atual   NUMERIC(10,3) NOT NULL DEFAULT 0,
    quantidade_minima  NUMERIC(10,3) NOT NULL DEFAULT 0,
    descricao          TEXT,
    ativo              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE movimentacoes_estoque (
    id                  BIGSERIAL     PRIMARY KEY,
    id_item             BIGINT        NOT NULL REFERENCES itens_estoque(id),
    tipo                VARCHAR(10)   NOT NULL,
    quantidade          NUMERIC(10,3) NOT NULL,
    data_movimentacao   TIMESTAMP     NOT NULL,
    motivo              VARCHAR(30)   NOT NULL,
    observacoes         TEXT,
    id_doacao           BIGINT        REFERENCES doacoes(id),
    id_animal           BIGINT        REFERENCES animais(id),
    id_responsavel      BIGINT        NOT NULL REFERENCES usuarios(id),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);