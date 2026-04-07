-- Usuários
CREATE INDEX idx_usuarios_email  ON usuarios(email);
CREATE INDEX idx_usuarios_cpf    ON usuarios(cpf);
CREATE INDEX idx_usuarios_perfil ON usuarios(perfil);

-- Animais
CREATE INDEX idx_animais_especie    ON animais(especie);
CREATE INDEX idx_animais_disponivel ON animais(disponivel);
CREATE INDEX idx_animais_porte      ON animais(porte);

-- Pedidos de adoção
CREATE INDEX idx_pedidos_status   ON pedidos_adocao(status);
CREATE INDEX idx_pedidos_adotante ON pedidos_adocao(id_adotante);
CREATE INDEX idx_pedidos_voluntario ON pedidos_adocao(id_voluntario_responsavel);

-- Doações
CREATE INDEX idx_doacoes_data     ON doacoes(data);
CREATE INDEX idx_doacoes_doador   ON doacoes(id_doador);
CREATE INDEX idx_doacoes_categoria ON doacoes(categoria);

-- Estoque
CREATE INDEX idx_movimentacoes_item ON movimentacoes_estoque(id_item);
CREATE INDEX idx_movimentacoes_data ON movimentacoes_estoque(data_movimentacao);
CREATE INDEX idx_itens_estoque_categoria ON itens_estoque(categoria);
CREATE INDEX idx_itens_estoque_ativo ON itens_estoque(ativo);

-- Perfil adotante
CREATE INDEX idx_perfis_usuario ON perfis_adotante(id_usuario);