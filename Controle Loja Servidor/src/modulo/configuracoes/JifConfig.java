package modulo.configuracoes;

import modulo.usuarios.UsuarioDAO;
import ConexaoData.Conexao;
import java.awt.Dimension;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modulo.campanhas.acompanhamento.JifAcompanhamentoCampanhas;
import modulo.metodos.ConvertMD5;
import modulo.metodos.CryptoBase64;
import modulo.versao.Versao;
import modulo.login.UserLogado;
import modulo.usuarios.Usuario;

/**
 *
 * @author Marcos Junior
 */
public final class JifConfig extends javax.swing.JInternalFrame {

    private String loja = "prop.server.loja";
    private String host = "prop.server.host";
    private String port = "prop.server.port";
    private String user = "prop.server.user";
    private String pass = "prop.server.password";
    private String base = "prop.server.base";
    private String impressora = "prop.server.impressora";
    private String backup = "prop.server.backup";
    private String diretorioRel = "prop.server.diretorioRel";
    private Properties properties;
    private ConvertMD5 md5;
    private Versao ver;
    private UserLogado login;
    private UsuarioDAO DAOUser;
    private Usuario userAut;
    private Config conf;
    private Conexao con;
    private CryptoBase64 cry;
    private String senhadoBanco = null;
    private String funcao = null;
    private String assistente = "ASSISTENTE DE GERENTE";
    private String gerente = "GERENTE";

    public JifConfig() {
        initComponents();
        properties = new Properties();
        md5 = new ConvertMD5();
        DAOUser = new UsuarioDAO();
        login = new UserLogado();
        ver = new Versao();
        cry = new CryptoBase64();
        setTitle("Configuração: " + ver.getVersao());
        Desabilita();
        try {
            CarregaDados();
            TestaConexao();
            iniciaDados();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao Carregar Arquivo!" + ex.getMessage());
        } catch (Exception ex) {
            jtStatus.setText("OFFLINE");
        }
        jtLoja.requestFocus();
        imprimeRelatorio();

    }

    public void TestaConexao() throws Exception {
        con = new Conexao();
        if (con.getCONEXAO().isValid(0)) {
            jtStatus.setText("ONLINE");
        } else {
            jtStatus.setText("OFFLINE");
        }
    }

    public void setPosicao() {
        Dimension d = this.getDesktopPane().getSize();
        this.setLocation((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2);
    }

    public void iniciaDados() throws Exception {
        int matricula = Integer.parseInt(login.getMatricula());
        userAut = DAOUser.PesquisaPorMatricula(matricula);
        senhadoBanco = userAut.getSenha();
        funcao = userAut.getFuncao();
        jtUserLogado.setText(Integer.toString(userAut.getMatricula()));
        jtFuncao.setText(userAut.getFuncao());
    }

    public void AutorizaEdicao() throws NoSuchAlgorithmException, Exception {
        String dig = JOptionPane.showInternalInputDialog(this, "Senha de Autorização");
        String senhaDiditada = md5.MD5String(dig);
        if (funcao.equals(assistente) || funcao.equals(gerente)) {
            if (senhadoBanco == null ? senhaDiditada == null : senhadoBanco.equals(senhaDiditada)) {
                Habilita();
            } else {
                JOptionPane.showMessageDialog(this, "Senha Incorreta!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Acesso Negado!");
        }
    }

    public void Habilita() {
        boolean h = true;
        jtLoja.setEnabled(h);
        jtHost.setEnabled(h);
        jtPort.setEnabled(h);
        jtUser.setEnabled(h);
        jtPass.setEnabled(h);
        jtBase.setEnabled(h);
        jbSalvar.setEnabled(h);
        jcListaImpressoras.setEnabled(h);
        jcBackups.setEnabled(h);
        jcDiretorioRelatorio.setEnabled(h);
        try {
            Decrypt();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Falha ao Descryptografar Dados!" + ex.getMessage());
        }
    }

    public void Desabilita() {
        boolean h = false;
        jtLoja.setEnabled(h);
        jtHost.setEnabled(h);
        jtPort.setEnabled(h);
        jtUser.setEnabled(h);
        jtPass.setEnabled(h);
        jtBase.setEnabled(h);
        jcListaImpressoras.setEnabled(h);
        jcBackups.setEnabled(h);
        jcDiretorioRelatorio.setEnabled(h);
    }

    public static Properties getProp() throws IOException {
        Properties props = new Properties();
        FileInputStream file = new FileInputStream(
                "./properties/dados.properties");
        props.load(file);
        return props;
    }

    public void CarregaDados() throws IOException {
        Properties prop = getProp();
        jtLoja.setText(prop.getProperty(loja));
        jtHost.setText(prop.getProperty(host));
        jtPort.setText(prop.getProperty(port));
        jtUser.setText(prop.getProperty(user));
        jtPass.setText(prop.getProperty(pass));
        jtBase.setText(prop.getProperty(base));
        jcListaImpressoras.setSelectedItem(prop.getProperty(impressora));
        jcBackups.setSelectedItem(prop.getProperty(backup));
        jcDiretorioRelatorio.setSelectedItem(prop.getProperty(diretorioRel));

    }

    public void Decrypt() throws IOException {
        Properties prop = getProp();
        jtLoja.setText(prop.getProperty(loja));
        jtHost.setText(cry.decrypt(prop.getProperty(host)));
        jtPort.setText(cry.decrypt(prop.getProperty(port)));
        jtUser.setText(cry.decrypt(prop.getProperty(user)));
        jtPass.setText(cry.decrypt(prop.getProperty(pass)));
        jtBase.setText(cry.decrypt(prop.getProperty(base)));
        jcListaImpressoras.setSelectedItem(prop.getProperty(impressora));
        jcBackups.setSelectedItem(prop.getProperty(backup));
        jcDiretorioRelatorio.setSelectedItem(prop.getProperty(diretorioRel));
    }

    public void EditaDados() throws FileNotFoundException, IOException {
        File file = new File("./properties/dados.properties");
        FileInputStream fis = new FileInputStream(file);
        properties.load(fis);
        properties.setProperty(loja, jtLoja.getText());
        properties.setProperty(host, cry.encrypt(jtHost.getText()));
        properties.setProperty(port, cry.encrypt(jtPort.getText()));
        properties.setProperty(user, cry.encrypt(jtUser.getText()));
        properties.setProperty(pass, cry.encrypt(jtPass.getText()));
        properties.setProperty(base, cry.encrypt(jtBase.getText()));

        properties.setProperty(impressora, jcListaImpressoras.getSelectedItem().toString());
        properties.setProperty(backup, jcBackups.getSelectedItem().toString());
        properties.setProperty(diretorioRel, jcDiretorioRelatorio.getSelectedItem().toString());
        FileOutputStream fos = new FileOutputStream(file);
        properties.store(fos, "FILE DADOS PROPERTIES:");
        fos.close();
        conf.CarregaDados();
    }

    public boolean VerificaCampos() {
        boolean chek = true;
        if (jtLoja.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo Loja!");
            chek = false;
        }
        if (jtHost.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo Host!");
            chek = false;
        }
        if (jtPort.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo Porta!");
            chek = false;
        }
        if (jtUser.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo User!");
            chek = false;
        }
        if (jtPass.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo Password!");
            chek = false;
        }
        if (jtBase.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Digite o campo Base!");
            chek = false;
        }
        return chek;
    }

    private void imprimeRelatorio() {
        PrintService[] pservices = PrinterJob.lookupPrintServices();
        if (pservices.length > 0) {
            for (PrintService ps : pservices) {
                jcListaImpressoras.addItem(ps.getName());
            }

        }
    }
//    private void listarImpressorasDisponíveis() {
//        PrintService[] pservices = PrinterJob.lookupPrintServices();
//        DefaultTableModel modelo = (DefaultTableModel) jTableImpressora.getModel();
//        modelo.setNumRows(0);
//        if (pservices.length > 0) {
//            for (PrintService ps : pservices) {
//                cont2++;
//                modelo.addRow(new String[]{
//                    Integer.toString(cont2), ps.getName()
//                });
//            }
//        } else {
//        }
//    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jtLoja = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jtHost = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jtPort = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jtUser = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jtPass = new javax.swing.JTextField();
        jtBase = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jtUserLogado = new javax.swing.JTextField();
        jtStatus = new javax.swing.JTextField();
        jtFuncao = new javax.swing.JTextField();
        jbEditar = new javax.swing.JButton();
        jbSalvar = new javax.swing.JButton();
        jbAtualizar = new javax.swing.JButton();
        jbCancelar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jcDiretorioRelatorio = new javax.swing.JComboBox<>();
        jcListaImpressoras = new javax.swing.JComboBox<>();
        jcBackups = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));

        jLabel1.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Loja");

        jtLoja.setBackground(new java.awt.Color(153, 255, 204));
        jtLoja.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtLoja.setForeground(new java.awt.Color(0, 0, 255));
        jtLoja.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtLoja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtLojaActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Host");

        jtHost.setBackground(new java.awt.Color(153, 255, 204));
        jtHost.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtHost.setForeground(new java.awt.Color(0, 0, 255));
        jtHost.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtHostActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Port");

        jtPort.setBackground(new java.awt.Color(153, 255, 204));
        jtPort.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtPort.setForeground(new java.awt.Color(0, 0, 255));
        jtPort.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtPortActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("User");

        jtUser.setBackground(new java.awt.Color(153, 255, 204));
        jtUser.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtUser.setForeground(new java.awt.Color(0, 0, 255));
        jtUser.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtUserActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Pass");

        jtPass.setBackground(new java.awt.Color(153, 255, 204));
        jtPass.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtPass.setForeground(new java.awt.Color(0, 0, 255));
        jtPass.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtPassActionPerformed(evt);
            }
        });

        jtBase.setBackground(new java.awt.Color(153, 255, 204));
        jtBase.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtBase.setForeground(new java.awt.Color(0, 0, 255));
        jtBase.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtBase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtBaseActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("BD");

        jLabel8.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Função");

        jLabel9.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Conexão");

        jLabel10.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Usuário");

        jtUserLogado.setEditable(false);
        jtUserLogado.setBackground(new java.awt.Color(204, 255, 153));
        jtUserLogado.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtUserLogado.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jtStatus.setEditable(false);
        jtStatus.setBackground(new java.awt.Color(51, 255, 51));
        jtStatus.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jtFuncao.setEditable(false);
        jtFuncao.setBackground(new java.awt.Color(204, 255, 153));
        jtFuncao.setFont(new java.awt.Font("sansserif", 1, 10)); // NOI18N
        jtFuncao.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jbEditar.setBackground(new java.awt.Color(51, 255, 102));
        jbEditar.setText("Editar");
        jbEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEditarActionPerformed(evt);
            }
        });

        jbSalvar.setBackground(new java.awt.Color(0, 255, 204));
        jbSalvar.setText("Salvar");
        jbSalvar.setEnabled(false);
        jbSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSalvarActionPerformed(evt);
            }
        });

        jbAtualizar.setBackground(new java.awt.Color(153, 153, 255));
        jbAtualizar.setText("Atualiza");
        jbAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAtualizarActionPerformed(evt);
            }
        });

        jbCancelar.setBackground(new java.awt.Color(0, 255, 204));
        jbCancelar.setText("Fechar");
        jbCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelarActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Impressora Padrão \"Sweda\"");

        jLabel11.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Diretórios dos Relatórios Gerados");

        jcDiretorioRelatorio.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jcDiretorioRelatorio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione um Diretório Disponível." }));

        jcListaImpressoras.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jcListaImpressoras.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione uma Impressora." }));

        jcBackups.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jcBackups.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione um Diretório para Backup Disponível." }));

        jLabel12.setFont(new java.awt.Font("Microsoft YaHei UI", 1, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Backups");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtLoja))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtHost))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtPort))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtUser))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jtPass)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jtUserLogado)
                                    .addComponent(jtFuncao)
                                    .addComponent(jtStatus)
                                    .addComponent(jtBase)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 472, Short.MAX_VALUE)
                                .addComponent(jbEditar)
                                .addGap(18, 18, 18)
                                .addComponent(jbAtualizar)
                                .addGap(18, 18, 18)
                                .addComponent(jbSalvar)
                                .addGap(18, 18, 18)
                                .addComponent(jbCancelar)))
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcDiretorioRelatorio, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcListaImpressoras, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jcBackups, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jtBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jtFuncao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(68, 68, 68))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jtLoja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel10)
                                .addComponent(jtUserLogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(jtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcDiretorioRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcListaImpressoras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcBackups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbEditar)
                    .addComponent(jbSalvar)
                    .addComponent(jbAtualizar)
                    .addComponent(jbCancelar))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbEditarActionPerformed
        try {
            AutorizaEdicao();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Falha Erro!" + ex.getMessage());
        }
    }//GEN-LAST:event_jbEditarActionPerformed

    private void jbSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSalvarActionPerformed
        if (VerificaCampos()) {
            try {
                EditaDados();
                Desabilita();
                JOptionPane.showMessageDialog(null, "Arquivo editado com sucesso!");
                jbSalvar.setEnabled(false);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Erro ao Salvar Arquivo!" + ex.getMessage());
            }
        }
    }//GEN-LAST:event_jbSalvarActionPerformed

    private void jbAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAtualizarActionPerformed
        try {
            CarregaDados();
            TestaConexao();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao Carregar o Arquivo!" + ex.getMessage());
        } catch (Exception ex) {
            jtStatus.setText("OFFLINE");
        }
    }//GEN-LAST:event_jbAtualizarActionPerformed

    private void jtLojaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtLojaActionPerformed
        jtHost.requestFocus();
    }//GEN-LAST:event_jtLojaActionPerformed

    private void jtHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtHostActionPerformed
        jtPort.requestFocus();
    }//GEN-LAST:event_jtHostActionPerformed

    private void jtPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtPortActionPerformed
        jtUser.requestFocus();
    }//GEN-LAST:event_jtPortActionPerformed

    private void jtUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtUserActionPerformed
        jtPass.requestFocus();
    }//GEN-LAST:event_jtUserActionPerformed

    private void jtPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtPassActionPerformed
        jtBase.requestFocus();
    }//GEN-LAST:event_jtPassActionPerformed

    private void jtBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtBaseActionPerformed
        jbSalvar.requestFocus();
    }//GEN-LAST:event_jtBaseActionPerformed

    private void jbCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelarActionPerformed
        this.dispose();
    }//GEN-LAST:event_jbCancelarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbAtualizar;
    private javax.swing.JButton jbCancelar;
    private javax.swing.JButton jbEditar;
    private javax.swing.JButton jbSalvar;
    private javax.swing.JComboBox<String> jcBackups;
    private javax.swing.JComboBox<String> jcDiretorioRelatorio;
    private javax.swing.JComboBox<String> jcListaImpressoras;
    private javax.swing.JTextField jtBase;
    private javax.swing.JTextField jtFuncao;
    private javax.swing.JTextField jtHost;
    private javax.swing.JTextField jtLoja;
    private javax.swing.JTextField jtPass;
    private javax.swing.JTextField jtPort;
    private javax.swing.JTextField jtStatus;
    private javax.swing.JTextField jtUser;
    private javax.swing.JTextField jtUserLogado;
    // End of variables declaration//GEN-END:variables
}
