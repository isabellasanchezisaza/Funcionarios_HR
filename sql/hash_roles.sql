INSERT INTO usuarios (username, password_hash, id_rol) VALUES
('admin',    'pbkdf2$600000$NxDg1/U6EAM6lYXCui2j7w==$fUv/CGbf/EmyZdjJfifTwZ+KjB8e9wPXBwBz+wIEqsI=',    (SELECT id FROM roles WHERE nombre = 'ADMIN')),
('rrhh1',    'pbkdf2$600000$o4/BdwMiZEdAyo+fG3C6EQ==$B6H5aYQ8sQmcVBsscBhaE6ZoRywwS3neA28tD+WeirY=',     (SELECT id FROM roles WHERE nombre = 'RRHH')),
('consulta1','pbkdf2$600000$JeLfYb3tXYAtKa4B614PSA==$epz/UzSWKrxpOR0KpC2V/oRa1qwf0POg6J9sxAd92KE=', (SELECT id FROM roles WHERE nombre = 'CONSULTOR'));