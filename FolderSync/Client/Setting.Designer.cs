namespace Client
{
    partial class Setting
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.TxtBxUsername = new System.Windows.Forms.TextBox();
            this.TxtBxPassword = new System.Windows.Forms.TextBox();
            this.TxtBxDeviceName = new System.Windows.Forms.TextBox();
            this.BtnOK = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(32, 26);
            this.label1.Name = "label_username";
            this.label1.Size = new System.Drawing.Size(58, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Username:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(32, 58);
            this.label2.Name = "label_password";
            this.label2.Size = new System.Drawing.Size(56, 13);
            this.label2.TabIndex = 1;
            this.label2.Text = "Password:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(32, 90);
            this.label3.Name = "label_device_name";
            this.label3.Size = new System.Drawing.Size(75, 13);
            this.label3.TabIndex = 2;
            this.label3.Text = "Device Name:";
            // 
            // TxtBxUsername
            // 
            this.TxtBxUsername.Location = new System.Drawing.Point(118, 26);
            this.TxtBxUsername.Name = "TxtBxUsername";
            this.TxtBxUsername.Size = new System.Drawing.Size(100, 20);
            this.TxtBxUsername.TabIndex = 3;
            // 
            // TxtBxPassword
            // 
            this.TxtBxPassword.Location = new System.Drawing.Point(118, 58);
            this.TxtBxPassword.Name = "TxtBxPassword";
            this.TxtBxPassword.Size = new System.Drawing.Size(100, 20);
            this.TxtBxPassword.TabIndex = 4;
            // 
            // TxtBxDeviceName
            // 
            this.TxtBxDeviceName.Location = new System.Drawing.Point(118, 90);
            this.TxtBxDeviceName.Name = "TxtBxDeviceName";
            this.TxtBxDeviceName.Size = new System.Drawing.Size(100, 20);
            this.TxtBxDeviceName.TabIndex = 5;
            // 
            // BtnOK
            // 
            this.BtnOK.Location = new System.Drawing.Point(118, 212);
            this.BtnOK.Name = "BtnOK";
            this.BtnOK.Size = new System.Drawing.Size(75, 23);
            this.BtnOK.TabIndex = 6;
            this.BtnOK.Text = "OK";
            this.BtnOK.UseVisualStyleBackColor = true;
            this.BtnOK.Click += new System.EventHandler(this.BtnOK_Click);
            // 
            // Setting
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.BtnOK);
            this.Controls.Add(this.TxtBxDeviceName);
            this.Controls.Add(this.TxtBxPassword);
            this.Controls.Add(this.TxtBxUsername);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Name = "Setting";
            this.Text = "Folder Sync Settings";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox TxtBxUsername;
        private System.Windows.Forms.TextBox TxtBxPassword;
        private System.Windows.Forms.TextBox TxtBxDeviceName;
        private System.Windows.Forms.Button BtnOK;
    }
}

