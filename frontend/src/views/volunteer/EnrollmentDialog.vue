<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline"> New Application </span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation>
          <v-row>
            <v-col cols="12">
              <v-text-field
                label="*Motivation"
                :rules="[(v) => !!v || 'Motivation is required']"
                required
                v-model="enrollment.motivation"
                data-cy="motivationInput"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-enrollment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          v-if="isMotivationValid()"
          color="blue-darken-1"
          variant="text"
          data-cy="saveEnrollment"
          @click="saveEnrollment"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { Vue, Component, Model, Prop } from 'vue-property-decorator';
import Enrollment from '@/models/enrollment/Enrollment';
import RemoteServices from '@/services/RemoteServices';
import Activity from '@/models/activity/Activity';

@Component({})
export default class EnrollmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  enrollment: Enrollment = new Enrollment();

  async created() {
    this.enrollment = new Enrollment();
    this.enrollment.activity = this.activity;
  }

  isMotivationValid() {
    if (this.enrollment.motivation == null) {
      return false;
    }
    return this.enrollment.motivation.trim().length >= 10;
  }

  async saveEnrollment() {
    if ((this.$refs.form as Vue & { validate: () => boolean }).validate()) {
      try {
        if (this.enrollment.activity.id !== null) {
          const result = await RemoteServices.enrollToActivity(
            this.enrollment.activity.id,
            this.enrollment,
          );
          this.$emit('save-enrollment', result);
        }
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style scoped lang="scss"></style>
